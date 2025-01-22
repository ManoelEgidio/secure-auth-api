package com.secure.auth_service.services;

import com.secure.auth_service.dtos.UserSummaryDTO;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new CustomException("Usuário já existe.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void update(UUID userId, UserSummaryDTO userDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
        if (!existingUser.getId().equals(userDTO.getId())) {
            throw new CustomException("ID informado não corresponde ao usuário encontrado.");
        }
        User updatedUser = userDTO.toEntity();
        updatedUser.setLogin(existingUser.getLogin());
        updatedUser.setPassword(existingUser.getPassword());
        userRepository.save(updatedUser);
    }

    @Transactional
    public void updatePassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public Optional<UserSummaryDTO> getUserSummary(UUID id) {
        return userRepository.findById(id)
                .map(UserSummaryDTO::new);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException("Usuário não encontrado.");
        }
        userRepository.deleteById(userId);
    }

    public Page<UserSummaryDTO> search(Map<String, Object> filters, Integer page, Integer size, String sort, String direction) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        return userRepository.search(filters, pageable).map(UserSummaryDTO::new);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username);
    }
}
