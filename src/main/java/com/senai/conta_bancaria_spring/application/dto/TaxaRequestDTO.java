package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TaxaRequestDTO(
        @NotBlank(message = "A descrição não pode ser vazia.")
        String descricao,

        @NotNull(message = "O percentual não pode ser nulo.")
        @PositiveOrZero(message = "O percentual deve ser positivo ou zero.")
        BigDecimal percentual,

        @NotNull(message = "O valor fixo não pode ser nulo.")
        @PositiveOrZero(message = "O valor fixo deve ser positivo ou zero.")
        BigDecimal valorFixo
) {
}
