package com.senai.conta_bancaria_spring.domain.exception;

public class ValorInvalidoException extends RuntimeException {
    public ValorInvalidoException(String message) {
        super(message);
    }
}
