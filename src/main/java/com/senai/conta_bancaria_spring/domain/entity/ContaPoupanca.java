package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Poupanca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ContaPoupanca extends Conta {

    private BigDecimal rendimento;

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