package com.senai.conta_bancaria_spring.interface_ui.exception;

import com.senai.conta_bancaria_spring.domain.exception.RecursoNaoEncontradoException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Anotação que centraliza o tratamento de exceções para toda a aplicação.
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Trata especificamente os erros de validação dos DTOs anotados com @Valid.
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(); // Cria um mapa para a resposta JSON de erros.
        ex.getBindingResult().getAllErrors().forEach((error) -> { // Itera sobre cada erro de validação encontrado.
            String fieldName = ((FieldError) error).getField(); // Obtém o nome do campo que falhou na validação.
            String errorMessage = error.getDefaultMessage(); // Obtém a mensagem de erro definida na anotação de validação.
            errors.put(fieldName, errorMessage); // Adiciona o campo e a mensagem de erro ao mapa.
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Retorna o mapa de erros com o status HTTP 400 (Bad Request).
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    // Trata exceções comuns de regras de negócio.
    public ResponseEntity<Object> handleBusinessException(Exception ex) {
        return new ResponseEntity<>(Map.of("erro", ex.getMessage()), HttpStatus.BAD_REQUEST); // Retorna a mensagem da exceção com status HTTP 400.
    }

    @ExceptionHandler(OptimisticLockingFailureException.class) // Trata a exceção de concorrência.
    public ResponseEntity<Object> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex) {
        return new ResponseEntity<>(
                Map.of("erro", "Conflito de concorrência: Os dados foram modificados por outra transação. Por favor, tente novamente."),
                HttpStatus.CONFLICT // Retorna o status 409 Conflict.
        );
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex) {
        return new ResponseEntity<>(Map.of("erro", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // --- O HANDLER MAIS GENÉRICO NO FINAL ---
    @ExceptionHandler(Exception.class) // Trata qualquer outra exceção não capturada pelos métodos acima.
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return new ResponseEntity<>(Map.of("erro", "Ocorreu um erro interno no servidor."), HttpStatus.INTERNAL_SERVER_ERROR); // Retorna uma mensagem genérica com status 500.
    }
}