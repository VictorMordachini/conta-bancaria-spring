package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Taxa;

import java.math.BigDecimal;

public record TaxaResponseDTO(
        String id,
        String descricao,
        BigDecimal percentual,
        BigDecimal valorFixo
) {
    // Método conversor estático para facilitar a transformação da Entidade para DTO
    public static TaxaResponseDTO fromEntity(Taxa taxa) {
        return new TaxaResponseDTO(
                taxa.getId(),
                taxa.getDescricao(),
                taxa.getPercentual(),
                taxa.getValorFixo()
        );
    }
}
