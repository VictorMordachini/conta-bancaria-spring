package com.senai.conta_bancaria_spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        // Esta lógica verifica se a exceção tem a nossa mensagem personalizada
        // (ex: "Você não é o proprietário") ou se deve usar a mensagem padrão.
        String mensagemErro = (accessDeniedException.getMessage() != null && !accessDeniedException.getMessage().equals("Access Denied"))
                ? accessDeniedException.getMessage()
                : "Acesso negado. Você não tem permissão para realizar esta operação.";

        Map<String, String> body = Map.of("erro", mensagemErro);

        // Escreve o JSON diretamente na resposta
        response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
    }
}