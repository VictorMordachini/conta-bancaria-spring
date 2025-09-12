package com.senai.conta_bancaria_spring.application.dto;

import lombok.Getter;

@Getter
public class ClienteRequestDTO {
    private String nome;
    private Long cpf;
    private String tipoConta; // "CORRENTE" ou "POUPANCA"
    private Double saldoInicial;
    private Long limite; // Apenas para Conta Corrente
    private Double taxa;   // Apenas para Conta Corrente
    private Double rendimento; // Apenas para Conta Poupança
}
