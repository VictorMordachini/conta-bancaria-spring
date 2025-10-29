package com.senai.conta_bancaria_spring.domain.exception;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String message) {
        super(message);
    }
}
