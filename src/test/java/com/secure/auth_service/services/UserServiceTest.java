package com.secure.auth_service.services;

import com.secure.auth_service.dtos.UserRegisterDTO;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

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
}
