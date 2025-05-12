package com.example.camunda2.registration_process.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloakUrl}")
    private String keycloakTokenUrl;

    @Value("${identity.clientId}")
    private String clientId;

    @Value("${identity.clientSecret}")
    private String clientSecret;

    @Value("${keycloak.admin.url:http://localhost:18080/auth/admin/realms/camunda-platform}")
    private String keycloakAdminUrl;

    private final RestTemplate restTemplate;
    
    // Cache to store users by group to avoid frequent API calls
    private final Map<String, List<String>> usersByGroupCache = new HashMap<>();
    private final Map<String, Integer> currentIndexByGroup = new HashMap<>();

    public KeycloakService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get an access token for Keycloak API calls
     */
    private String getKeycloakAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                keycloakTokenUrl,
                request,
                Map.class);

        return (String) response.getBody().get("access_token");
    }

    /**
     * Get all users in a specific group
     */
    public List<String> getUsersByGroup(String groupName) {
        // Check cache first
        if (usersByGroupCache.containsKey(groupName)) {
            return usersByGroupCache.get(groupName);
        }

        try {
            String accessToken = getKeycloakAccessToken();
            
            // First, find the group ID by name
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> groupsResponse = restTemplate.exchange(
                    keycloakAdminUrl + "/groups",
                    HttpMethod.GET,
                    entity,
                    List.class);
            
            String groupId = null;
            for (Object group : groupsResponse.getBody()) {
                Map<String, Object> groupMap = (Map<String, Object>) group;
                if (groupName.equals(groupMap.get("name"))) {
                    groupId = (String) groupMap.get("id");
                    break;
                }
            }
            
            if (groupId == null) {
                return new ArrayList<>();
            }
            
            // Now get users in this group
            ResponseEntity<List> usersResponse = restTemplate.exchange(
                    keycloakAdminUrl + "/groups/" + groupId + "/members",
                    HttpMethod.GET,
                    entity,
                    List.class);
            
            List<String> users = new ArrayList<>();
            for (Object user : usersResponse.getBody()) {
                Map<String, Object> userMap = (Map<String, Object>) user;
                users.add((String) userMap.get("username"));
            }
            
            // Cache the result
            usersByGroupCache.put(groupName, users);
            return users;
        } catch (Exception e) {
            // In case of error, return a default list or empty list
            System.err.println("Error fetching users from Keycloak: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get the next user in round-robin fashion from a specific group
     */
    public String getNextUserFromGroup(String groupName) {
        List<String> users = getUsersByGroup(groupName);
        
        if (users.isEmpty()) {
            return null;
        }
        
        // Get current index for this group
        int currentIndex = currentIndexByGroup.getOrDefault(groupName, 0);
        
        // Get next user
        String nextUser = users.get(currentIndex);
        
        // Update index for next time
        currentIndex = (currentIndex + 1) % users.size();
        currentIndexByGroup.put(groupName, currentIndex);
        
        return nextUser;
    }
    
    /**
     * Refresh the cache for a specific group
     */
    public void refreshGroupCache(String groupName) {
        usersByGroupCache.remove(groupName);
        getUsersByGroup(groupName);
    }
    
    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        usersByGroupCache.clear();
        currentIndexByGroup.clear();
    }
}