package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.DiscriminatorValue;

import lombok.Getter;
import lombok.Setter;

@DiscriminatorValue("Poupanca")
@Getter
@Setter
public class ContaPoupanca extends Conta {

    private Double rendimento;

    public ContaPoupanca(Long numero, Double saldo, Double rendimento) {
        super(numero, saldo);
        this.rendimento = rendimento;
    }

    @Override
    public void sacar(Double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que R$0,00.");
        }
        if (valor > this.getSaldo()) {
            throw new IllegalStateException("Saldo insuficiente.");
        }
        this.setSaldo(this.getSaldo() - valor);
    }

    public void aplicarRendimento() {
        double valorRendimento = this.getSaldo() * this.rendimento;
        this.setSaldo(this.getSaldo() + valorRendimento);
    }
}
