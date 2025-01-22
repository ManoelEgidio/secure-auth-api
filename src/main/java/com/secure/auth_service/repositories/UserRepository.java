package com.secure.auth_service.repositories;

import com.secure.auth_service.models.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u WHERE u.login = :login")
    org.springframework.security.core.userdetails.UserDetails findByLogin(String login);
    boolean existsByLogin(String login);

    default Page<User> search(Map<String, Object> filters, Pageable pageable) {
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (filters.containsKey("name")) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("name"), "%" + filters.get("name") + "%"));
            }
            if (filters.containsKey("login")) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("login"), filters.get("login")));
            }
            if (filters.containsKey("role")) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("role"), filters.get("role")));
            }

            return predicate;
        };

        return findAll(specification, pageable);
    }
}
