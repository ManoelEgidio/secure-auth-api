package com.secure.auth_service.controllers;

import com.secure.auth_service.dtos.AuthenticationDTO;
import com.secure.auth_service.dtos.UserRegisterDTO;
import com.secure.auth_service.dtos.UserSummaryDTO;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUpSecurityContext() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test Admin")
                .login("admin.user@example.com")
                .role(Roles.ADMIN)
                .authorities(Set.of(Authority.CREATE, Authority.EDIT, Authority.DISABLE, Authority.VIEW, Authority.SEARCH))
                .enabled(true)
                .build();

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (GrantedAuthority authority : user.getAuthorities()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                grantedAuthorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void register_Success() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setName("João Silva");
        userRegisterDTO.setLogin("joao.silva@example.com");
        userRegisterDTO.setPassword("Senha123");
        userRegisterDTO.setRole(Roles.USER);
        userRegisterDTO.setAuthorities(Set.of(Authority.CREATE));

        when(userService.register(any())).thenReturn(null);

        assertDoesNotThrow(() -> userController.register(userRegisterDTO));

        verify(userService, times(1)).register(any());
    }

    @Test
    void register_UserAlreadyExists() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setName("João Silva");
        userRegisterDTO.setLogin("joao.silva@example.com");
        userRegisterDTO.setPassword("Senha123");
        userRegisterDTO.setRole(Roles.USER);
        userRegisterDTO.setAuthorities(Set.of(Authority.CREATE));

        doThrow(new CustomException("Usuário já existe.")).when(userService).register(any());

        CustomException exception = assertThrows(CustomException.class, () -> userController.register(userRegisterDTO));
        assertEquals("Usuário já existe.", exception.getMessage());

        verify(userService, times(1)).register(any());
    }

    @Test
    void update_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("João Silva")
                .login("joao.silva@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.EDIT))
                .enabled(true)
                .build();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(user);
        userSummaryDTO.setName("João Silva Atualizado");

        doNothing().when(userService).update(eq(userId), any());

        assertDoesNotThrow(() -> userController.update(userId, userSummaryDTO));

        verify(userService, times(1)).update(eq(userId), any());
    }

    @Test
    void update_UserNotFound() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("João Silva")
                .login("joao.silva@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.EDIT))
                .enabled(true)
                .build();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(user);
        userSummaryDTO.setName("João Silva Atualizado");

        doThrow(new CustomException("Usuário não encontrado.")).when(userService).update(eq(userId), any());

        CustomException exception = assertThrows(CustomException.class, () -> userController.update(userId, userSummaryDTO));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userService, times(1)).update(eq(userId), any());
    }

    @Test
    void updatePassword_Success() {
        UUID userId = UUID.randomUUID();
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setLogin("joao.silva@example.com");
        authDTO.setPassword("NovaSenha123");

        doNothing().when(userService).updatePassword(eq(userId), anyString());

        assertDoesNotThrow(() -> userController.updatePassword(userId, authDTO));

        verify(userService, times(1)).updatePassword(eq(userId), anyString());
    }

    @Test
    void updatePassword_MissingLoginOrPassword() {
        UUID userId = UUID.randomUUID();
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setLogin(null);
        authDTO.setPassword(null);

        CustomException exception = assertThrows(CustomException.class, () -> userController.updatePassword(userId, authDTO));
        assertEquals("Login e senha são obrigatórios para alteração.", exception.getMessage());

        verify(userService, times(0)).updatePassword(any(), anyString());
    }

    @Test
    void disableUser_Success() {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).disableUser(eq(userId));

        assertDoesNotThrow(() -> userController.disableUser(userId));

        verify(userService, times(1)).disableUser(eq(userId));
    }

    @Test
    void disableUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        doThrow(new CustomException("Usuário não encontrado.")).when(userService).disableUser(eq(userId));

        CustomException exception = assertThrows(CustomException.class, () -> userController.disableUser(userId));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userService, times(1)).disableUser(eq(userId));
    }

    @Test
    void getById_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("João Silva")
                .login("joao.silva@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(user);

        when(userService.getUserSummary(eq(userId))).thenReturn(Optional.of(userSummaryDTO));

        UserSummaryDTO result = userController.getById(userId);
        assertNotNull(result);
        assertEquals(userId, result.getId());

        verify(userService, times(1)).getUserSummary(eq(userId));
    }

    @Test
    void getById_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userService.getUserSummary(eq(userId))).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userController.getById(userId));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userService, times(1)).getUserSummary(eq(userId));
    }

    @Test
    void delete_Success() {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).deleteUser(eq(userId));

        assertDoesNotThrow(() -> userController.delete(userId));

        verify(userService, times(1)).deleteUser(eq(userId));
    }

    @Test
    void delete_UserNotFound() {
        UUID userId = UUID.randomUUID();

        doThrow(new CustomException("Usuário não encontrado.")).when(userService).deleteUser(eq(userId));

        CustomException exception = assertThrows(CustomException.class, () -> userController.delete(userId));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userService, times(1)).deleteUser(eq(userId));
    }

    @Test
    void search_Success() {
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .login("joao.silva@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(user1);
        Page<UserSummaryDTO> page = new PageImpl<>(List.of(userSummaryDTO));

        when(userService.search(anyMap(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search("João", "joao.silva@example.com", Roles.USER, 0, 20, "name", "asc");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void search_NameOnly() {
        Page<UserSummaryDTO> page = new PageImpl<>(Collections.emptyList());

        when(userService.search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"))).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search("João", null, null, 0, 20, "name", "asc");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"));
    }

    @Test
    void search_LoginOnly() {
        Page<UserSummaryDTO> page = new PageImpl<>(Collections.emptyList());

        when(userService.search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"))).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search(null, "joao.silva@example.com", null, 0, 20, "name", "asc");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"));
    }

    @Test
    void search_RoleOnly() {
        Page<UserSummaryDTO> page = new PageImpl<>(Collections.emptyList());

        when(userService.search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"))).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search(null, null, Roles.USER, 0, 20, "name", "asc");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"));
    }

    @Test
    void search_NoFilters() {
        Page<UserSummaryDTO> page = new PageImpl<>(Collections.emptyList());

        when(userService.search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"))).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search(null, null, null, 0, 20, "name", "asc");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), eq(0), eq(20), eq("name"), eq("asc"));
    }

    @Test
    void search_InvalidSortDirection() {
        Page<UserSummaryDTO> page = new PageImpl<>(Collections.emptyList());

        when(userService.search(anyMap(), eq(0), eq(20), eq("name"), eq("desc"))).thenReturn(page);

        Page<UserSummaryDTO> result = userController.search(null, null, null, 0, 20, "name", "desc");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).search(anyMap(), eq(0), eq(20), eq("name"), eq("desc"));
    }
}
