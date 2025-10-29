package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Transacao;
import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponseDTO(
        LocalDateTime dataHora,
        TipoTransacao tipo,
        BigDecimal valor,
        Long contaDestinoNumero
) {
    public static TransacaoResponseDTO fromEntity(Transacao transacao) {
        // Chamada mais limpa para o construtor do record
        return new TransacaoResponseDTO(
                transacao.getDataHora(),
                transacao.getTipo(),
                transacao.getValor(),
                transacao.getContaDestinoNumero()
        );
    }
}