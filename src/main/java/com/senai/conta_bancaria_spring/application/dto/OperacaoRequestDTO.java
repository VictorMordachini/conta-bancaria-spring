package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;


public record OperacaoRequestDTO(
        @NotNull(message = "O valor não pode ser nulo.")
        @Positive(message = "O valor da operação deve ser positivo.")
        BigDecimal valor
) {

}