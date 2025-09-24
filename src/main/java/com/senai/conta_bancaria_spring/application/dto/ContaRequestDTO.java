package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ContaRequestDTO {
    @NotBlank(message = "O tipo de conta não pode ser vazio.")
    @Pattern(regexp = "^(Corrente|Poupanca)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Tipo de conta inválido. Use 'Corrente' ou 'Poupanca'.")
    private String tipoConta;

    @NotNull(message = "O saldo inicial não pode ser nulo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial não pode ser negativo.")
    private BigDecimal saldoInicial;

    // Campos condicionais para Conta Corrente
    private Long limite;
    private BigDecimal taxa;

    // Campo condicional para Conta Poupança
    private BigDecimal rendimento;
}
