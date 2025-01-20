package com.secure.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationDTO {

    @Schema(description = "Login do usuário", example = "usuario@example.com", required = true)
    @NotBlank
    private String login;

    @Schema(description = "Senha do usuário", example = "Senha123", required = true)
    @NotBlank
    private String password;
}
