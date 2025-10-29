package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferenciaRequestDTO(
        @NotNull(message = "O número da conta de destino não pode ser nulo.")
        @Positive(message = "O número da conta de destino deve ser um número positivo.")
        Long numeroContaDestino,

        @NotNull(message = "O valor não pode ser nulo.")
        @Positive(message = "O valor da transferência deve ser positivo.")
        BigDecimal valor
) {

}
