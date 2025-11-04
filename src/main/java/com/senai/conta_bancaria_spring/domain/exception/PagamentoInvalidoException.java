package com.senai.conta_bancaria_spring.domain.exception;

// Exceção usada para regras de negócio específicas de pagamento,
// como boleto vencido ou código de barras inválido.
public class PagamentoInvalidoException extends RuntimeException {
    public PagamentoInvalidoException(String message) {
        super(message);
    }
}
