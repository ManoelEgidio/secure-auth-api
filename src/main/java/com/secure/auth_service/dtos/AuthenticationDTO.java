package com.secure.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AuthenticationDTO {

    @Schema(description = "ID do usuário", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Login do usuário", example = "usuario@example.com", required = true)
    @NotNull(message = "O campo 'login' não pode ser nulo.")
    public String login;

    @Schema(description = "Senha do usuário", example = "Senha123", required = true)
    @NotNull(message = "O campo 'password' não pode ser nulo.")
    public String password;
}
