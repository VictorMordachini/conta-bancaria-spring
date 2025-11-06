package com.senai.conta_bancaria_spring.domain.exception;

public class AutenticacaoIoTExpiradaException extends RuntimeException {
    public AutenticacaoIoTExpiradaException(String message) {
        super(message);
    }
}
