package com.secure.auth_service.utils;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @BeforeEach
    void setUpSecurityContext() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test Admin")
                .login("admin.user@example.com")
                .role(Roles.ADMIN) // Define o role necessário
                .authorities(Set.of(Authority.CREATE, Authority.EDIT, Authority.DISABLE, Authority.VIEW, Authority.SEARCH))
                .enabled(true)
                .build();

        Set<SimpleGrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toSet());

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                grantedAuthorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkRoleAndAuthority_Success() {
        assertDoesNotThrow(() -> SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.CREATE));
    }

    @Test
    void checkRoleAndAuthority_Failure() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();

        Set<SimpleGrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toSet());

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())); // e.g., "ROLE_USER"

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                grantedAuthorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomException exception = assertThrows(CustomException.class, () ->
                SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.CREATE)
        );
        assertTrue(exception.getMessage().contains("Acesso negado: Role insuficiente."));
    }

    @Test
    void checkRoleAndAuthority_Unauthenticated() {
        SecurityContextHolder.clearContext();

        CustomException exception = assertThrows(CustomException.class, () ->
                SecurityUtils.checkRoleAndAuthority(Roles.ADMIN, Authority.CREATE)
        );
        assertTrue(exception.getMessage().contains("Usuário não autenticado."));
    }
}
