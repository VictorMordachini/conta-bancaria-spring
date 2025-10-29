package com.senai.conta_bancaria_spring.domain.exception;

public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
