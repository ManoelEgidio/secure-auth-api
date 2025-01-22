package com.secure.auth_service.Models;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test User")
                .login("test@example.com")
                .password("password123")
                .role(Roles.USER)
                .authorities(Set.of(Authority.CREATE, Authority.VIEW))
                .enabled(true)
                .build();
    }

    @Test
    void testGetAuthorities() {
        Set<String> authorityNames = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("CREATE", "VIEW"), authorityNames);
    }

    @Test
    void testGetUsername() {
        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(user.isEnabled());
    }
}
