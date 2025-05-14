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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.url}")
    private String keycloakUrl;
    
    @Value("${keycloak.admin.url}")
    private String keycloakAdminTokenUrl;

    @Value("${identity.clientId}")
    private String clientId;

    @Value("${identity.clientSecret}")
    private String clientSecret;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private final RestTemplate restTemplate;
    
    private final Map<String, Integer> currentIndexByGroup = new HashMap<>();

    public KeycloakService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get an access token for Keycloak API calls
     */
    private String getKeycloakAccessToken() {
        try {
            System.out.println("Getting Keycloak admin access token from: " + keycloakAdminTokenUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", "admin-cli");  // Use admin-cli client for admin operations
            map.add("username", adminUsername);
            map.add("password", adminPassword);
    
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    keycloakAdminTokenUrl,
                    request,
                    Map.class);
            
            String token = (String) response.getBody().get("access_token");
            System.out.println("Successfully obtained admin access token");
            return token;
        } catch (Exception e) {
            System.err.println("Error getting Keycloak admin access token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all users in a specific group
     */
    public List<String> getUsersByGroup(String groupName) {
    

        try {
            System.out.println("Fetching users for group: " + groupName);
            String accessToken = getKeycloakAccessToken();
            System.out.println("Got admin access token");
            
            // First, find the group ID by name
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String groupsUrl = keycloakUrl + "/groups";
            System.out.println("Fetching groups from: " + groupsUrl);
            
            ResponseEntity<List> groupsResponse = restTemplate.exchange(
                    groupsUrl,
                    HttpMethod.GET,
                    entity,
                    List.class);
            
            System.out.println("Found " + (groupsResponse.getBody() != null ? groupsResponse.getBody().size() : 0) + " groups");
            
            String groupId = null;
            if (groupsResponse.getBody() != null) {
                for (Object group : groupsResponse.getBody()) {
                    Map<String, Object> groupMap = (Map<String, Object>) group;
                    System.out.println("Group: " + groupMap.get("name") + ", ID: " + groupMap.get("id"));
                    if (groupName.equals(groupMap.get("name"))) {
                        groupId = (String) groupMap.get("id");
                        break;
                    }
                }
            }
            
            // Now get users in this group
            String usersUrl = keycloakUrl + "/groups/" + groupId + "/members";
            System.out.println("Fetching users from: " + usersUrl);
            
            ResponseEntity<List> usersResponse = restTemplate.exchange(
                    usersUrl,
                    HttpMethod.GET,
                    entity,
                    List.class);
            
            List<String> users = new ArrayList<>();
            if (usersResponse.getBody() != null) {
                for (Object user : usersResponse.getBody()) {
                    Map<String, Object> userMap = (Map<String, Object>) user;
                    String username = (String) userMap.get("username");
                    System.out.println("Found user: " + username);
                    users.add(username);
                }
            }
            
            System.out.println("Total users found for group " + groupName + ": " + users.size());
            
            return users;
        } catch (Exception e) {
            // In case of error, print detailed error information
            System.err.println("Error fetching users from Keycloak: " + e.getMessage());
            e.printStackTrace();
    
            return null;
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
}