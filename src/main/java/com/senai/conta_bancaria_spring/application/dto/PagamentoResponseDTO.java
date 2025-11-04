package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import com.senai.conta_bancaria_spring.domain.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        String id,
        StatusPagamento status,
        BigDecimal valorPago, // Valor principal do boleto
        BigDecimal valorTotalTaxas,
        BigDecimal custoTotal, // Soma do valorPago + valorTotalTaxas
        LocalDateTime dataPagamento,
        Long contaNumero
) {
    /**
     * Construtor estático para criar o DTO
     *
     * @param pagamento  A entidade Pagamento persistida
     * @param custoTotal O custo total (boleto + taxas) que foi retornado pelo DomainService
     */
    public static PagamentoResponseDTO fromEntity(Pagamento pagamento, BigDecimal custoTotal) {
        // O custoTotal é a soma, o valorPago é o base. A diferença é o total de taxas.
        BigDecimal totalTaxas = (custoTotal != null)
                ? custoTotal.subtract(pagamento.getValorPago())
                : BigDecimal.ZERO;

        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getStatus(),
                pagamento.getValorPago(),
                totalTaxas,
                (custoTotal != null) ? custoTotal : pagamento.getValorPago(), // Custo total é 0 em falhas
                pagamento.getDataPagamento(),
                pagamento.getConta().getNumero()
        );
    }
}
