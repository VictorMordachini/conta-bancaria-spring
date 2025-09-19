package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@DiscriminatorValue("Corrente") // Valor que identifica esta classe na coluna 'tipo_conta'
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
public class ContaCorrente extends Conta {

    @Column(nullable = false)
    private Long limite;

    @Column(nullable = false)
    private BigDecimal taxa;

    public ContaCorrente(Long numero, BigDecimal saldo, Long limite, BigDecimal taxa) {
        super(numero, saldo);
        this.limite = limite;
        this.taxa = taxa;
    }

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
}
