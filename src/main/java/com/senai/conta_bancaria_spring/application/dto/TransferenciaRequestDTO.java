package com.senai.conta_bancaria_spring.application.dto;

import lombok.Getter;

@Getter
public class TransferenciaRequestDTO {
    private Long numeroContaDestino;
    private Double valor;
}
