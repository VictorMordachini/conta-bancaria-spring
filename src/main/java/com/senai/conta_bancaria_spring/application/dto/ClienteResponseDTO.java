package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;

import java.util.List;
import java.util.stream.Collectors;

public record ClienteResponseDTO(
        String id,
        String nome,
        Long cpf,
        List<ContaResponseDTO> contas
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        List<ContaResponseDTO> contasDto = cliente.getContas().stream()
                .map(ContaResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                contasDto
        );
    }
}