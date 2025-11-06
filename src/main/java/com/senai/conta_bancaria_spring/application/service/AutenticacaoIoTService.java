package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.iot.AutenticacaoSolicitacaoDTO;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.exception.AutenticacaoIoTExpiradaException;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import com.senai.conta_bancaria_spring.infrastructure.mqtt.MqttPublisherService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AutenticacaoIoTService {
    private final CodigoAutenticacaoRepository codigoRepository;
    private final MqttPublisherService mqttPublisher;
    private final Random random = new Random();

    public AutenticacaoIoTService(CodigoAutenticacaoRepository codigoRepository, MqttPublisherService mqttPublisher) {
        this.codigoRepository = codigoRepository;
        this.mqttPublisher = mqttPublisher;
    }

    @Transactional
    public String solicitarAutenticacao(Cliente cliente) {
        // 1. Gerar código aleatório de 6 dígitos
        String codigo = String.format("%06d", random.nextInt(999999));

        // 2. Salvar no banco (expira em 5 minutos)
        CodigoAutenticacao entidade = CodigoAutenticacao.builder()
                .cliente(cliente)
                .codigo(codigo)
                .expiraEm(LocalDateTime.now().plusMinutes(5))
                .validado(false)
                .build();
        CodigoAutenticacao salvo = codigoRepository.save(entidade);

        // 3. Publicar MQTT
        AutenticacaoSolicitacaoDTO payload = new AutenticacaoSolicitacaoDTO(cliente.getId(), codigo);
        mqttPublisher.solicitarAutenticacao(payload);

        return salvo.getId(); // Retorna o ID do código gerado para controle futuro
    }

    @Transactional
    public Optional<CodigoAutenticacao> validarCodigoRecebido(String clienteId, String codigoRecebido) {
        // Busca o código mais recente, não validado, deste cliente
        Optional<CodigoAutenticacao> codigoOpt = codigoRepository
                .findFirstByClienteIdAndValidadoFalseOrderByExpiraEmDesc(clienteId);

        if (codigoOpt.isPresent()) {
            CodigoAutenticacao entidade = codigoOpt.get();

            // VERIFICAÇÃO DE EXPIRAÇÃO EXPLÍCITA
            if (entidade.getExpiraEm().isBefore(LocalDateTime.now())) {
                throw new AutenticacaoIoTExpiradaException("O tempo limite para autenticação biométrica expirou.");
            }

            // Verifica se o código bate
            if (entidade.getCodigo().equals(codigoRecebido)) {
                entidade.setValidado(true);
                codigoRepository.save(entidade);
                System.out.println(">>> SUCESSO: Código IoT validado para cliente " + clienteId);
                return Optional.of(entidade);
            }
        }
        System.out.println(">>> FALHA: Tentativa de validação IoT falhou ou código inexistente para cliente " + clienteId);
        return Optional.empty();
    }
}
