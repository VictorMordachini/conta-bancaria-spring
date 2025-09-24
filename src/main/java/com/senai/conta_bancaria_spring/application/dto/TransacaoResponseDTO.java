package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Transacao;
import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransacaoResponseDTO {
    private LocalDateTime dataHora;
    private TipoTransacao tipo;
    private BigDecimal valor;
    private Long contaDestinoNumero; // Será nulo se não for uma transferência

    // Método de fábrica para converter a entidade Transacao neste DTO.
    public static TransacaoResponseDTO fromEntity(Transacao transacao) {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setDataHora(transacao.getDataHora());
        dto.setTipo(transacao.getTipo());
        dto.setValor(transacao.getValor());
        dto.setContaDestinoNumero(transacao.getContaDestinoNumero());
        return dto;
    }
}
