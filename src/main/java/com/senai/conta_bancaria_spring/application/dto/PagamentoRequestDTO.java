package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record PagamentoRequestDTO(
        @NotBlank(message = "O código do boleto não pode ser vazio.")
        String codigoBoleto,

        @NotNull(message = "O valor não pode ser nulo.")
        @Positive(message = "O valor do pagamento deve ser positivo.")
        BigDecimal valor,

        // Lista de IDs (UUIDs) das taxas que devem ser aplicadas
        List<String> idsTaxas
) {
}
