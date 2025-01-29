package com.secure.auth_service.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

public record EmailDTO (
        @NotEmpty String to,
        @NotEmpty String subject,
        @NotEmpty String text
) implements Serializable { }
