package com.senai.conta_bancaria_spring.application.dto;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ClienteResponseDTO {
    private String id;
    private String nome;
    private Long cpf;
    private List<ContaResponseDTO> contas;

    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setContas(
                cliente.getContas().stream()
                        .map(ContaResponseDTO::fromEntity)
                        .collect(Collectors.toList())
        );
        return dto;
    }

}
