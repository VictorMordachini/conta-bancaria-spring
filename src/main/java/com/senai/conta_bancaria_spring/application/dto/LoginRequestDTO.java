package com.senai.conta_bancaria_spring.application.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(@NotNull(message = "O CPF não pode ser nulo.") Long cpf,
                              @NotNull(message = "A senha não pode ser nula.") String senha) {
}
