package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ContaCorrenteUpdateRequestDTO(
        // Os campos são opcionais, mas se forem informados, devem ser válidos.
        @PositiveOrZero(message = "O limite deve ser um valor positivo ou zero.")
        Long limite,

        @Positive(message = "A taxa deve ser um valor positivo.")
        BigDecimal taxa
) {
}