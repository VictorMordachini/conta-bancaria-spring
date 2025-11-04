package com.senai.conta_bancaria_spring.domain.enums;

public enum StatusPagamento {
    PENDENTE,
    SUCESSO,
    FALHA_SALDO_INSUFICIENTE, //
    FALHA_BOLETO_VENCIDO,     // [cite: 20]
    FALHA_OPERACIONAL         // Um status genérico para outros erros
}
