package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.DiscriminatorValue;

import lombok.Getter;
import lombok.Setter;



@DiscriminatorValue("Corrente") // Valor que identifica esta classe na coluna 'tipo_conta'
@Getter
@Setter
public class ContaCorrente extends Conta{

    private Long limite;
    private Double taxa;

    public ContaCorrente(Long numero, Double saldo, Long limite, Double taxa) {
        super(numero, saldo);
        this.limite = limite;
        this.taxa = taxa;
    }

    @Override
    public void sacar(Double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que R$0,00.");
        }
        double saldoDisponivel = this.getSaldo() + this.limite;
        if (valor > saldoDisponivel) {
            throw new IllegalStateException("Saldo insuficiente, mesmo com o limite.");
        }

        double valorComTaxa = valor + (valor * this.taxa);
        if (valorComTaxa > saldoDisponivel) {
            throw new IllegalStateException("Saldo insuficiente para cobrir o saque mais a taxa.");
        }

        this.setSaldo(this.getSaldo() - valorComTaxa);
    }
}
