package com.senai.conta_bancaria_spring.interface_ui.exception;

import com.senai.conta_bancaria_spring.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Anotação que centraliza o tratamento de exceções para toda a aplicação.
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Trata especificamente os erros de validação dos DTOs anotados com @Valid.
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>(); // Cria um mapa para a resposta JSON de erros.
        ex.getBindingResult().getAllErrors().forEach((error) -> { // Itera sobre cada erro de validação encontrado.
            String fieldName = ((FieldError) error).getField(); // Obtém o nome do campo que falhou na validação.
            String errorMessage = error.getDefaultMessage(); // Obtém a mensagem de erro definida na anotação de validação.
            errors.put(fieldName, errorMessage); // Adiciona o campo e a mensagem de erro ao mapa.
        });

        ApiErrorDTO errorDTO = new ApiErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Um ou mais campos falharam na validação.",
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST); // Retorna o DTO padronizado.
    }

    @ExceptionHandler({IllegalArgumentException.class,
            IllegalStateException.class,
            SaldoInsuficienteException.class,
            ValorInvalidoException.class,
            RegraDeNegocioException.class,
            PagamentoInvalidoException.class
    })
    // Trata exceções comuns de regras de negócio.
    public ResponseEntity<ApiErrorDTO> handleBusinessException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(OptimisticLockingFailureException.class) // Trata a exceção de concorrência.
    public ResponseEntity<ApiErrorDTO> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex, HttpServletRequest request) {
        String mensagem = "Conflito de concorrência: Os dados foram modificados por outra transação. Por favor, tente novamente.";

        ApiErrorDTO errorDTO = new ApiErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                mensagem,
                request.getRequestURI(),
                null
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT); // Retorna o status 409 Conflict.
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorDTO> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }


    // --- O HANDLER MAIS GENÉRICO NO FINAL ---
    @ExceptionHandler(Exception.class) // Trata qualquer outra exceção não capturada pelos métodos acima.
    public ResponseEntity<ApiErrorDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        // Logar a exceção real para depuração interna
        // log.error("Erro interno não tratado: {}", ex.getMessage(), ex); // (Se tivéssemos @Slf4j aqui)

        String mensagem = "Ocorreu um erro interno no servidor.";
        ApiErrorDTO errorDTO = new ApiErrorDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                mensagem,
                request.getRequestURI(),
                null
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR); // Retorna uma mensagem genérica com status 500.
    }

    // Método utilitário para criar respostas de erro
    private ResponseEntity<ApiErrorDTO> buildErrorResponse(Exception ex, HttpStatus status, HttpServletRequest request) {
        ApiErrorDTO errorDTO = new ApiErrorDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(), // ex: "Bad Request", "Not Found"
                ex.getMessage(), // Mensagem da exceção
                request.getRequestURI(),
                null
        );
        return new ResponseEntity<>(errorDTO, status);
    }
}