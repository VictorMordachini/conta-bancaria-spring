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
        validarValorDebitoPositivo(valor, "saque");
        validarSaldoSuficiente(valor);

        this.setSaldo(this.getSaldo().subtract(valor));
    }

    @Override
    public BigDecimal debitarParaTransferencia(BigDecimal valor) {
        this.sacar(valor);
        return valor;
    }

    public void aplicarRendimento() {
        BigDecimal valorRendimento = this.getSaldo().multiply(this.rendimento);
        this.setSaldo(this.getSaldo().add(valorRendimento));
    }


}