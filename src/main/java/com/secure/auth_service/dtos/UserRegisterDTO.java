package com.secure.auth_service.dtos;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserRegisterDTO {

    @Schema(description = "ID do usuário", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Nome completo do usuário", example = "João Silva", required = true)
    @NotBlank
    private String name;

    @Schema(description = "Email do usuário", example = "joao.silva@example.com", required = true)
    @Email
    @NotBlank
    private String login;

    @Schema(description = "Senha do usuário", example = "Senha123", required = true)
    @NotBlank
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    private String password;

    @Schema(description = "Cargo do usuário", example = "USER", required = true)
    @NotBlank
    private Roles role;

    @Schema(description = "Permissões do usuário", example = "[\"CREATE\", \"UPDATE\"]", required = true)
    private Set<Authority> authorities;

    public User toEntity() {
        return User.builder()
                .name(this.name)
                .login(this.login)
                .password(this.password)
                .role(this.role)
                .authorities(this.authorities)
                .enabled(true)
                .build();
    }
}
