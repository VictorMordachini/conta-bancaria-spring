package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ClienteRequestDTO {

    @NotBlank(message = "O nome não pode ser vazio ou nulo.") // Para Strings, @NotBlank é melhor que @NotNull.
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotNull(message = "O CPF não pode ser nulo.")
    @Positive(message = "O CPF deve ser um número positivo.")
    private Long cpf;

    @NotBlank(message = "O tipo de conta não pode ser vazio.")
    @Pattern(regexp = "^(Corrente|Poupanca)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Tipo de conta inválido. Use 'Corrente' ou 'Poupanca'.")
    private String tipoConta; // "CORRENTE" ou "POUPANCA"

    @NotNull(message = "O saldo inicial não pode ser nulo.")
    @DecimalMin(value = "10.0", inclusive = true, message = "O saldo inicial não pode ser menor do que R$10,00.")
    private BigDecimal saldoInicial;

    @Positive(message = "O limite, se informado, deve ser positivo.")
    private Long limite; // Apenas para Conta Corrente

    @Positive(message = "A taxa deve ser positiva.")
    private BigDecimal taxa;   // Apenas para Conta Corrente

    @Positive(message = "O rendiment deve ser positivo.")
    private BigDecimal rendimento; // Apenas para Conta Poupança
}
