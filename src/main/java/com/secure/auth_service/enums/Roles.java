package com.secure.auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@AllArgsConstructor
public enum Roles {

    ADMIN("ADMIN"),
    USER("USER");

    private final String roleName;

}
