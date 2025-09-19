package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@DiscriminatorValue("Poupanca")
@Entity
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class ContaPoupanca extends Conta {

    @Column(nullable = false)
    private BigDecimal rendimento;

    public ContaPoupanca(Long numero, BigDecimal saldo, BigDecimal rendimento) {
        super(numero, saldo);
        this.rendimento = rendimento;
    }

    @Override
    public void sacar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que R$0,00.");
        }
        if (valor.compareTo(this.getSaldo()) > 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }
        this.setSaldo(this.getSaldo().subtract(valor));
    }

    public void aplicarRendimento() {
        BigDecimal valorRendimento = this.getSaldo().multiply(this.rendimento);
        this.setSaldo(this.getSaldo().add(valorRendimento));
    }
}
