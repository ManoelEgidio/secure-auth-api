//package com.secure.auth_service.repositories;
//
//import com.secure.auth_service.enums.Authority;
//import com.secure.auth_service.enums.Roles;
//import com.secure.auth_service.models.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.*;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class UserRepositoryTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User user1, user2, user3;
//
//    private int getRoleOrdinal(Roles role) {
//        return role.ordinal();
//    }
//
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//
//        user1 = User.builder()
//                .name("Alice Silva")
//                .login("alice@example.com")
//                .password("passwordAlice")
//                .role(Roles.ADMIN)
//                .authorities(Set.of(Authority.VIEW))
//                .enabled(true)
//                .build();
//
//        user2 = User.builder()
//                .name("Bob Souza")
//                .login("bob@example.com")
//                .password("passwordBob")
//                .role(Roles.USER)
//                .authorities(Set.of(Authority.VIEW))
//                .enabled(true)
//                .build();
//
//        user3 = User.builder()
//                .name("Charlie Pereira")
//                .login("charlie@example.com")
//                .password("passwordCharlie")
//                .role(Roles.USER)
//                .authorities(Set.of(Authority.VIEW))
//                .enabled(false)
//                .build();
//
//        userRepository.saveAll(Arrays.asList(user1, user2, user3));
//    }
//
//    @Test
//    @DisplayName("Deve encontrar usuário pelo login")
//    void testFindByLogin() {
//        Optional<User> foundUser = userRepository.findByLogin("alice@example.com");
//        assertTrue(foundUser.isPresent());
//        assertEquals("Alice Silva", foundUser.get().getName());
//    }
//
//    @Test
//    @DisplayName("Deve retornar vazio quando o login não existir")
//    void testFindByLogin_NotFound() {
//        Optional<User> foundUser = userRepository.findByLogin("nonexistent@example.com");
//        assertFalse(foundUser.isPresent());
//    }
//
//    @Test
//    @DisplayName("Deve verificar existência de usuário pelo login")
//    void testExistsByLogin() {
//        assertTrue(userRepository.existsByLogin("bob@example.com"));
//        assertFalse(userRepository.existsByLogin("unknown@example.com"));
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuários com filtros de nome")
//    void testSearch_FilterByName() {
//        Map<String, Object> filters = new HashMap<>();
//        filters.put("name", "Alice");
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(1, result.getTotalElements());
//        assertEquals("Alice Silva", result.getContent().get(0).getName());
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuários com filtros de login")
//    void testSearch_FilterByLogin() {
//        Map<String, Object> filters = new HashMap<>();
//        filters.put("login", "bob@example.com");
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("login").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(1, result.getTotalElements());
//        assertEquals("Bob Souza", result.getContent().get(0).getName());
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuários com filtros de role")
//    void testSearch_FilterByRole() {
//        Map<String, Object> filters = new HashMap<>();
//        filters.put("role", Roles.USER);
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("role").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(2, result.getTotalElements());
//        List<String> names = Arrays.asList(result.getContent().get(0).getName(), result.getContent().get(1).getName());
//        assertTrue(names.contains("Bob Souza"));
//        assertTrue(names.contains("Charlie Pereira"));
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuários com múltiplos filtros")
//    void testSearch_MultipleFilters() {
//        Map<String, Object> filters = new HashMap<>();
//        filters.put("role", Roles.USER);
//        filters.put("name", "Charlie");
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(1, result.getTotalElements());
//        assertEquals("Charlie Pereira", result.getContent().get(0).getName());
//    }
//
//    @Test
//    @DisplayName("Deve buscar todos os usuários quando não há filtros")
//    void testSearch_NoFilters() {
//        Map<String, Object> filters = new HashMap<>();
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(3, result.getTotalElements());
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuários com paginação correta")
//    void testSearch_Pagination() {
//        Map<String, Object> filters = new HashMap<>();
//
//        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());
//        Page<User> result = userRepository.search(filters, pageable);
//
//        assertEquals(3, result.getTotalElements());
//        assertEquals(2, result.getContent().size());
//
//        pageable = PageRequest.of(1, 2, Sort.by("name").ascending());
//        result = userRepository.search(filters, pageable);
//
//        assertEquals(3, result.getTotalElements());
//        assertEquals(1, result.getContent().size());
//    }
//}
