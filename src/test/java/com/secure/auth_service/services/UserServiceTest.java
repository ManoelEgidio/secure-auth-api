package com.secure.auth_service.services;

import com.secure.auth_service.dtos.UserRegisterDTO;
import com.secure.auth_service.dtos.UserSummaryDTO;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegisterDTO userRegisterDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setName("João Silva");
        userRegisterDTO.setLogin("joao.silva@example.com");
        userRegisterDTO.setPassword("Senha123");
        userRegisterDTO.setRole(Roles.USER);
        userRegisterDTO.setAuthorities(Set.of(Authority.CREATE));

        user = spy(User.builder()
                .id(UUID.randomUUID())
                .name(userRegisterDTO.getName())
                .login(userRegisterDTO.getLogin())
                .password("EncodedSenha123")
                .role(userRegisterDTO.getRole())
                .authorities(userRegisterDTO.getAuthorities())
                .enabled(true)
                .build());
    }

    @Test
    void register_Success() {
        when(userRepository.existsByLogin(userRegisterDTO.getLogin())).thenReturn(false);
        when(passwordEncoder.encode(userRegisterDTO.getPassword())).thenReturn("EncodedSenha123");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.register(userRegisterDTO.toEntity());

        assertNotNull(registeredUser);
        assertEquals("João Silva", registeredUser.getName());
        assertEquals("EncodedSenha123", registeredUser.getPassword());

        verify(userRepository, times(1)).existsByLogin(userRegisterDTO.getLogin());
        verify(passwordEncoder, times(1)).encode(userRegisterDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExists() {
        when(userRepository.existsByLogin(userRegisterDTO.getLogin())).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> userService.register(userRegisterDTO.toEntity()));
        assertEquals("Usuário já existe.", exception.getMessage());

        // Verificações dos mocks
        verify(userRepository, times(1)).existsByLogin(userRegisterDTO.getLogin());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        UUID userId = user.getId();

        when(userRepository.existsById(userId)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> userService.deleteUser(userId));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_Success() throws NoSuchFieldException, IllegalAccessException {
        UUID userId = user.getId();
        UserSummaryDTO userDTO = new UserSummaryDTO(user);
        userDTO.setName("Novo Nome");
        userDTO.setAuthorities(Set.of(Authority.CREATE, Authority.DISABLE));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(userId, userDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        Field authoritiesField = User.class.getDeclaredField("authorities");
        authoritiesField.setAccessible(true);
        Set<Authority> actualAuthorities = (Set<Authority>) authoritiesField.get(savedUser);

        Set<Authority> expectedAuthorities = Set.of(Authority.CREATE, Authority.DISABLE);

        assertEquals("Novo Nome", savedUser.getName(), "O nome do usuário não foi atualizado corretamente.");
        assertEquals(user.getLogin(), savedUser.getLogin(), "O login do usuário deve permanecer inalterado.");
        assertEquals(user.getPassword(), savedUser.getPassword(), "A senha do usuário deve permanecer inalterada.");
        assertEquals(expectedAuthorities, actualAuthorities, "As autoridades do usuário não foram atualizadas corretamente.");
    }

    @Test
    void update_UserNotFound() {
        UUID userId = UUID.randomUUID();
        UserSummaryDTO userDTO = new UserSummaryDTO(user);
        userDTO.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userService.update(userId, userDTO));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_IdMismatch() {
        UUID userId = user.getId();
        UUID differentId = UUID.randomUUID();
        UserSummaryDTO userDTO = new UserSummaryDTO(user);
        userDTO.setId(differentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        CustomException exception = assertThrows(CustomException.class, () -> userService.update(userId, userDTO));
        assertEquals("ID informado não corresponde ao usuário encontrado.", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePassword_Success() {
        UUID userId = user.getId();
        String newPassword = "NovaSenha123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("EncodedNovaSenha123");

        userService.updatePassword(userId, newPassword);

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);

        assertEquals("EncodedNovaSenha123", user.getPassword());
    }

    @Test
    void updatePassword_UserNotFound() {
        UUID userId = UUID.randomUUID();
        String newPassword = "NovaSenha123";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userService.updatePassword(userId, newPassword));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void disableUser_Success() {
        UUID userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.disableUser(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertFalse(savedUser.isEnabled());
    }

    @Test
    void disableUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userService.disableUser(userId));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserSummary_UserFound() {
        UUID userId = user.getId();
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserSummaryDTO> result = userService.getUserSummary(userId);

        assertTrue(result.isPresent());
        assertEquals(userSummaryDTO.getId(), result.get().getId());
        assertEquals(userSummaryDTO.getName(), result.get().getName());
        assertEquals(userSummaryDTO.getLogin(), result.get().getLogin());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserSummary_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserSummaryDTO> result = userService.getUserSummary(userId);

        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void search_WithFilters_ReturnsPagedResults() {
        Map<String, Object> filters = Map.of("name", "João");
        int page = 0;
        int size = 10;
        String sort = "name";
        String direction = "ASC";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.search(filters, pageable)).thenReturn(userPage);

        Page<UserSummaryDTO> result = userService.search(filters, page, size, sort, direction);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(user.getName(), result.getContent().get(0).getName());

        verify(userRepository, times(1)).search(filters, pageable);
    }

    @Test
    void search_NoFilters_ReturnsAllUsersPaged() {
        Map<String, Object> filters = Map.of();
        int page = 0;
        int size = 10;
        String sort = "id";
        String direction = "DESC";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.search(filters, pageable)).thenReturn(userPage);

        Page<UserSummaryDTO> result = userService.search(filters, page, size, sort, direction);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(user.getId(), result.getContent().get(0).getId());

        verify(userRepository, times(1)).search(filters, pageable);
    }

    @Test
    void search_InvalidSortDirection_ThrowsException() {
        Map<String, Object> filters = Map.of();
        int page = 0;
        int size = 10;
        String sort = "name";
        String direction = "INVALID";

        assertThrows(IllegalArgumentException.class, () -> {
            userService.search(filters, page, size, sort, direction);
        });

        verify(userRepository, never()).search(anyMap(), any(Pageable.class));
    }
}
