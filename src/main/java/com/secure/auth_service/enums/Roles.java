package com.secure.auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@AllArgsConstructor
public enum Roles {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String roleName;

    public SimpleGrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(this.roleName);
    }
}
