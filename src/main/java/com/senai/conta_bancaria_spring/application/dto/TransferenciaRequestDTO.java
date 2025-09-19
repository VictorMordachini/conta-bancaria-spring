package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferenciaRequestDTO {

    @NotNull(message = "O número da conta de destino não pode ser nulo.")
    @Positive(message = "O número da conta de destino deve ser um número positivo.")
    private Long numeroContaDestino;

    @NotNull(message = "O valor não pode ser nulo.")
    @Positive(message = "O valor da transferência deve ser positivo.")
    private BigDecimal valor;
}
