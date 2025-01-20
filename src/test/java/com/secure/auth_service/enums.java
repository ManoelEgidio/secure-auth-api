package com.secure.auth_service;

import com.secure.auth_service.enums.Roles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void rolesEnum_ContainsAllValues() {
        assertNotNull(Roles.valueOf("ADMIN"));
        assertNotNull(Roles.valueOf("USER"));
    }
}
