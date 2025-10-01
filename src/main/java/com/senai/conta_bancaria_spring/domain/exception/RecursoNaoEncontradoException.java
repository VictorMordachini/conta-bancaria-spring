package com.senai.conta_bancaria_spring.domain.exception;

public class RecursoNaoEncontradoException extends  RuntimeException{
    public RecursoNaoEncontradoException(String message){
        super(message);
    }
}
