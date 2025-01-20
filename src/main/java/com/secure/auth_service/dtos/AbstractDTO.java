package com.secure.auth_service.dtos;

import com.secure.auth_service.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public abstract class AbstractDTO {

    @Schema(description = "ID da entidade", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Data de criação da entidade", example = "2025-01-01")
    private LocalDate createdAt;

    @Schema(description = "Data de atualização da entidade", example = "2025-01-10")
    private LocalDate updatedAt;

    @Schema(description = "Usuário que criou a entidade", example = "João Silva")
    private String createdBy;

    @Schema(description = "Usuário que atualizou a entidade pela última vez", example = "Maria Souza")
    private String updatedBy;
}
