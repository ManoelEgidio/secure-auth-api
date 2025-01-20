package com.secure.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StandardErrorDTO {

    @Schema(description = "Timestamp do erro", example = "1682547890123")
    private long timestamp;

    @Schema(description = "Código de status HTTP", example = "400")
    private int status;

    @Schema(description = "Razão do status", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "Mensagem de erro", example = "Usuário já existe.")
    private String message;

    @Schema(description = "URI da requisição", example = "/auth/register")
    private String path;
}
