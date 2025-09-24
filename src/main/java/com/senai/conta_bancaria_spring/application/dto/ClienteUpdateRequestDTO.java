package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ClienteUpdateRequestDTO {
    @NotBlank(message = "O nome não pode ser vazio.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    // Outros campos que pudessem ser atualizados (ex: endereço, telefone) entrariam aqui.
}


