package com.senai.conta_bancaria_spring.application.dto;

import java.time.LocalDateTime;

public record NotificacaoDTO(
        LocalDateTime timestamp,
        TipoNotificacao tipo,
        String mensagem
) {
    public enum TipoNotificacao {
        SUCESSO,
        FALHA,
        INFO
    }

    // Método estático para facilitar a criação de DTOs de sucesso
    public static NotificacaoDTO sucesso(String mensagem) {
        return new NotificacaoDTO(LocalDateTime.now(), TipoNotificacao.SUCESSO, mensagem);
    }

    // Método estático para facilitar a criação de DTOs de falha
    public static NotificacaoDTO falha(String mensagem) {
        return new NotificacaoDTO(LocalDateTime.now(), TipoNotificacao.FALHA, mensagem);
    }
}
