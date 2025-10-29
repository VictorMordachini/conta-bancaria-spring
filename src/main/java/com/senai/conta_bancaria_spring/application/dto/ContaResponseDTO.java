package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.entity.ContaPoupanca;

import java.math.BigDecimal;

public record ContaResponseDTO(
        Long numero,
        BigDecimal saldo,
        String tipoConta,
        Long limite,
        BigDecimal taxa,
        BigDecimal rendimento
) {
    public static ContaResponseDTO fromEntity(Conta conta) {
        Long numero = conta.getNumero();
        BigDecimal saldo = conta.getSaldo();
        String tipoConta;
        Long limite = null;
        BigDecimal taxa = null;
        BigDecimal rendimento = null;

        if (conta instanceof ContaCorrente cc) {
            tipoConta = "CORRENTE";
            limite = cc.getLimite();
            taxa = cc.getTaxa();
        } else if (conta instanceof ContaPoupanca cp) {
            tipoConta = "POUPANCA";
            rendimento = cp.getRendimento();
        } else {
            // Lida com um caso inesperado, se houver
            tipoConta = "DESCONHECIDO";
        }

        return new ContaResponseDTO(numero, saldo, tipoConta, limite, taxa, rendimento);
    }
}