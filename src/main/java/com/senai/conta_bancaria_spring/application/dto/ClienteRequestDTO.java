package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome não pode ser vazio ou nulo.")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String nome,

        @NotNull(message = "O CPF não pode ser nulo.")
        @Positive(message = "O CPF deve ser um número positivo.")
        Long cpf,

        @NotBlank(message = "A senha não pode ser vazia.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha,

        @NotBlank(message = "O tipo de conta não pode ser vazio.")
        @Pattern(regexp = "^(Corrente|Poupanca)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Tipo de conta inválido. Use 'Corrente' ou 'Poupanca'.")
        String tipoConta,

        @NotNull(message = "O saldo inicial não pode ser nulo.")
        @DecimalMin(value = "10.0", inclusive = true, message = "O saldo inicial não pode ser menor do que R$10,00.")
        BigDecimal saldoInicial,

        @Positive(message = "O limite, se informado, deve ser positivo.")
        Long limite, // Apenas para Conta Corrente

        @Positive(message = "A taxa deve ser positiva.")
        BigDecimal taxa,   // Apenas para Conta Corrente

        @Positive(message = "O rendiment deve ser positivo.")
        BigDecimal rendimento // Apenas para Conta Poupança
) {

}