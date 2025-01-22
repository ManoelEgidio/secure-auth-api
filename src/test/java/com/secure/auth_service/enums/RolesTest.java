package com.secure.auth_service.enums;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void rolesEnum_ContainsAllValues() {
        assertNotNull(Roles.valueOf("ADMIN"));
        assertNotNull(Roles.valueOf("USER"));
    }

    @Test
    void getAuthority_ReturnsCorrectGrantedAuthority() {
        SimpleGrantedAuthority adminAuthority = Roles.ADMIN.getAuthority();
        SimpleGrantedAuthority userAuthority = Roles.USER.getAuthority();

        assertNotNull(adminAuthority);
        assertNotNull(userAuthority);

        assertEquals("ROLE_ADMIN", adminAuthority.getAuthority());
        assertEquals("ROLE_USER", userAuthority.getAuthority());
    }
}
