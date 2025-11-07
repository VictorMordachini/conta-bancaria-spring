package com.senai.conta_bancaria_spring.interface_ui.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,

        @JsonInclude(JsonInclude.Include.NON_NULL) // Não inclui o campo se for nulo
        Map<String, String> validationErrors
) {
}
