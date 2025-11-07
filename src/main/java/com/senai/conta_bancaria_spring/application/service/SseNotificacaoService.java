package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.NotificacaoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseNotificacaoService {
    // Armazena os emitters (conexões) de cada cliente. Thread-safe.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Timeout de 30 minutos para a conexão SSE
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Cria e armazena um novo SseEmitter para um cliente.
     * Este método será chamado pelo NotificacaoController.
     */
    public SseEmitter criarEmitter(String clienteId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Adiciona o emitter ao mapa
        this.emitters.put(clienteId, emitter);
        log.info("SSE Emitter criado para o cliente: {}", clienteId);

        // Define o que fazer quando a conexão é completada (timeout ou erro)
        emitter.onCompletion(() -> {
            this.emitters.remove(clienteId);
            log.info("SSE Emitter removido (onCompletion) para o cliente: {}", clienteId);
        });
        emitter.onTimeout(() -> {
            this.emitters.remove(clienteId);
            log.info("SSE Emitter removido (onTimeout) para o cliente: {}", clienteId);
        });
        emitter.onError((e) -> {
            this.emitters.remove(clienteId);
            log.error("SSE Emitter removido (onError) para o cliente: {}. Erro: {}", clienteId, e.getMessage());
        });

        // Envia um evento "heartbeat" inicial para confirmar a conexão
        try {
            emitter.send(SseEmitter.event().name("connect").data("Conexão SSE estabelecida."));
        } catch (IOException e) {
            log.warn("Falha ao enviar evento de conexão SSE para cliente {}: {}", clienteId, e.getMessage());
        }

        return emitter;
    }

    /**
     * Envia uma notificação para um cliente específico.
     * Este método será chamado pelo MqttListenerService.
     */
    public void enviarNotificacao(String clienteId, NotificacaoDTO notificacao) {
        SseEmitter emitter = emitters.get(clienteId);

        if (emitter != null) {
            try {
                log.info("Enviando notificação SSE para cliente {}: {}", clienteId, notificacao.mensagem());
                // O 'eventName' "operacao_concluida" será usado pelo front-end para
                // filtrar este evento de outros eventos (como o "connect").
                SseEmitter.SseEventBuilder sseEvent = SseEmitter.event()
                        .name("operacao_concluida")
                        .data(notificacao);

                emitter.send(sseEvent);

            } catch (IOException e) {
                log.warn("Falha ao enviar notificação SSE para cliente {}. Removendo emitter. Erro: {}", clienteId, e.getMessage());
                // Se falhar, a conexão provavelmente está morta. Removemos.
                this.emitters.remove(clienteId);
            }
        } else {
            log.warn("Nenhum Emitter SSE ativo encontrado para o cliente: {}", clienteId);
        }
    }
}
