package com.example.camunda2.registration_process.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRoleConverterTests {

    private KeycloakRoleConverter converter;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        converter = new KeycloakRoleConverter();
        jwt = mock(Jwt.class);
    }

    @Test
    void convert_withRoles_returnsCorrectAuthorities() {
        // Arrange
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("admin", "user"));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", realmAccess);
        
        when(jwt.getClaims()).thenReturn(claims);
        
        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        
        // Assert
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_admin")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_user")));
    }

    @Test
    void convert_withoutRoles_returnsEmptyList() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        when(jwt.getClaims()).thenReturn(claims);
        
        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        
        // Assert
        assertTrue(authorities.isEmpty());
    }

    @Test
    void convert_withEmptyRoles_returnsEmptyList() {
        // Arrange
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", realmAccess);
        
        when(jwt.getClaims()).thenReturn(claims);
        
        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        
        // Assert
        assertTrue(authorities.isEmpty());
    }
}