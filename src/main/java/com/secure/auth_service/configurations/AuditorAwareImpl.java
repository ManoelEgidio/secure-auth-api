package com.secure.auth_service.configurations;

import com.secure.auth_service.models.User;
import com.secure.auth_service.repositories.UserRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorAwareImpl {

    @Bean
    public AuditorAware<User> auditorProvider() {
        return new AuditorAware<>() {
            @NotNull
            @Override
            public Optional<User> getCurrentAuditor() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof User) {
                        User auditor = (User) principal;
                        if (auditor.getId() != null) {
                            return Optional.of(auditor);
                        }
                    }
                }
                return Optional.empty();
            }
        };
    }
}


