package com.secure.auth_service.dtos;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AuthorizationDTO {

    @Schema(description = "ID do usuário", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Role do usuário", example = "admin", required = true)
    @NotNull(message = "O campo 'login' não pode ser nulo.")
    public Roles role;

    @Schema(description = "Permissões do usuário", example = "[\"CREATE\", \"UPDATE\"]", required = true)
    @NotNull(message = "O campo 'password' não pode ser nulo.")
    public Authority authority;
}
