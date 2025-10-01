package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Corrente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ContaCorrente extends Conta {

    private Long limite;

    private BigDecimal taxa;

    @Override
    public void sacar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que R$0,00.");
        }

        BigDecimal saldoDisponivel = this.getSaldo().add(BigDecimal.valueOf(this.limite));
        if (valor.compareTo(saldoDisponivel) > 0) {
            throw new IllegalStateException("Saldo insuficiente, mesmo com o limite.");
        }

        BigDecimal valorComTaxa = valor.add(valor.multiply(this.taxa));
        if (valorComTaxa.compareTo(saldoDisponivel) > 0) {
            throw new IllegalStateException("Saldo insuficiente para cobrir o saque mais a taxa.");
        }

        this.setSaldo(this.getSaldo().subtract(valorComTaxa));
    }

    @Override
    public BigDecimal debitarParaTransferencia(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que R$0,00.");
        }

        // A lógica de calcular o valor com taxa
        BigDecimal valorComTaxa = valor.add(valor.multiply(this.getTaxa()));
        BigDecimal saldoDisponivel = this.getSaldo().add(BigDecimal.valueOf(this.getLimite()));

        if (valorComTaxa.compareTo(saldoDisponivel) > 0) {
            throw new IllegalStateException("Saldo insuficiente para cobrir a transferência mais a taxa.");
        }

        // Debita o valor total (transferência + taxa).
        this.setSaldo(this.getSaldo().subtract(valorComTaxa));

        // Retorna o valor que foi efetivamente debitado.
        return valorComTaxa;
    }
}