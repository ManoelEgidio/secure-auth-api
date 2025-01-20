package com.secure.auth_service.utils;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUtils {

    /**
     * Verifica se o usuário atual possui a role necessária.
     *
     * @param requiredRole A role necessária para acessar o recurso.
     */
    public static void checkRole(Roles requiredRole) {
        Authentication authentication = getAuthentication();
        Set<Roles> userRoles = authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(authority -> Roles.valueOf(authority.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toSet());

        if (!userRoles.contains(requiredRole)) {
            throw new CustomException("Acesso negado: Role insuficiente.");
        }
    }

    /**
     * Verifica se o usuário atual possui a authority necessária.
     *
     * @param requiredAuthority A authority necessária para acessar o recurso.
     */
    public static void checkAuthority(Authority requiredAuthority) {
        Authentication authentication = getAuthentication();
        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!userAuthorities.contains(requiredAuthority.getAuthority())) {
            throw new CustomException("Acesso negado: Permissão insuficiente.");
        }
    }

    /**
     * Verifica se o usuário atual possui a role e a authority necessárias.
     *
     * @param requiredRole      A role necessária.
     * @param requiredAuthority A authority necessária.
     */
    public static void checkRoleAndAuthority(Roles requiredRole, Authority requiredAuthority) {
        checkRole(requiredRole);
        checkAuthority(requiredAuthority);
    }

    /**
     * Obtém o objeto Authentication atual.
     *
     * @return O objeto Authentication do contexto de segurança.
     */
    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("Usuário não autenticado.");
        }
        return authentication;
    }
}
