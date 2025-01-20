package com.secure.auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Authority {
    CREATE("CREATE"),
    EDIT("EDIT"),
    DISABLE("DISABLE"),
    VIEW("VIEW"),
    SEARCH("SEARCH");

    private final String authority;

    public String getAuthority() {
        return authority;
    }
}
