package com.senai.conta_bancaria_spring.domain.entity;

import com.senai.conta_bancaria_spring.application.dto.ContaCorrenteUpdateRequestDTO;
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
        validarValorDebitoPositivo(valor, "saque");

        BigDecimal valorComTaxa = valor.add(valor.multiply(this.taxa));
        validarSaldoComLimiteSuficiente(valorComTaxa, this.getLimite());

        this.setSaldo(this.getSaldo().subtract(valorComTaxa));
    }

    @Override
    public BigDecimal debitarParaTransferencia(BigDecimal valor) {
        validarValorDebitoPositivo(valor, "transferência");

        BigDecimal valorComTaxa = valor.add(valor.multiply(this.getTaxa()));
        validarSaldoComLimiteSuficiente(valorComTaxa, this.getLimite());

        this.setSaldo(this.getSaldo().subtract(valorComTaxa));
        return valorComTaxa;
    }

    public void atualizarParametros(ContaCorrenteUpdateRequestDTO dto) {
        if (dto.limite() != null) {
            this.setLimite(dto.limite());
        }
        if (dto.taxa() != null) {
            this.setTaxa(dto.taxa());
        }
    }

    /**
     * Sobrescreve o método de debitar pagamento para aplicar
     * a lógica de validação com limite, específica da Conta Corrente.
     */
    @Override
    public void debitarPagamento(BigDecimal valorTotal) {
        // 1. Valida se o valor do débito é positivo
        validarValorDebitoPositivo(valorTotal, "pagamento");

        // 2. Validação específica com LIMITE
        validarSaldoComLimiteSuficiente(valorTotal, this.getLimite());

        // 3. Aplica o débito
        this.setSaldo(this.getSaldo().subtract(valorTotal));
    }
}