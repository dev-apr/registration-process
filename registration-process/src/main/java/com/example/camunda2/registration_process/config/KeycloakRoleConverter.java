package com.example.camunda2.registration_process.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var roles = ((Collection<String>) ((Map<String, Object>) jwt.getClaims()
            .getOrDefault("realm_access", Map.of()))
            .getOrDefault("roles", List.of()));

        return roles.stream()
            .map(role -> "ROLE_" + role)  // Spring expects ROLE_ prefix
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
