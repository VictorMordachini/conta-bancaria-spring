package com.senai.conta_bancaria_spring.infrastructure.mqtt;

import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import com.senai.conta_bancaria_spring.application.dto.iot.AutenticacaoSolicitacaoDTO;
import org.springframework.stereotype.Service;

@Service
public class MqttPublisherService {
    // A biblioteca publicará automaticamente o retorno deste método no tópico.
    // Adaptamos para um tópico geral devido a limitações da biblioteca.
    @MqttPublisher("banco/autenticacao")
    public AutenticacaoSolicitacaoDTO solicitarAutenticacao(AutenticacaoSolicitacaoDTO payload) {
        return payload;
    }
}
