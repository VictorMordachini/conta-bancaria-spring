package com.senai.conta_bancaria_spring.infrastructure.mqtt;

import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import com.senai.conta_bancaria_spring.application.dto.PagamentoRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.iot.AutenticacaoValidacaoDTO;
import com.senai.conta_bancaria_spring.application.service.AutenticacaoIoTService;
import com.senai.conta_bancaria_spring.application.service.PagamentoAppService;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.entity.TransacaoPendente;
import com.senai.conta_bancaria_spring.domain.exception.AutenticacaoIoTExpiradaException;
import com.senai.conta_bancaria_spring.domain.repository.TransacaoPendenteRepository;
import com.senai.conta_bancaria_spring.domain.service.ContaServiceDomain;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.senai.conta_bancaria_spring.application.dto.NotificacaoDTO;
import com.senai.conta_bancaria_spring.application.service.SseNotificacaoService;


import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class MqttListenerService {

    private final AutenticacaoIoTService autenticacaoIoTService;
    private final TransacaoPendenteRepository transacaoPendenteRepository;
    private final ContaServiceDomain contaServiceDomain;
    private final PagamentoAppService pagamentoAppService;
    private final SseNotificacaoService notificacaoService;

    public MqttListenerService(AutenticacaoIoTService autenticacaoIoTService,
                               TransacaoPendenteRepository transacaoPendenteRepository,
                               ContaServiceDomain contaServiceDomain,
                               PagamentoAppService pagamentoAppService,
                               SseNotificacaoService notificacaoService) {
        this.autenticacaoIoTService = autenticacaoIoTService;
        this.transacaoPendenteRepository = transacaoPendenteRepository;
        this.contaServiceDomain = contaServiceDomain;
        this.pagamentoAppService = pagamentoAppService;
        this.notificacaoService = notificacaoService;
    }

    @MqttSubscriber("banco/validacao/+")
    @Transactional
    public void processarValidacaoIoT(@MqttPayload AutenticacaoValidacaoDTO payload) {
        log.info(">>> MQTT RECEBIDO: Validação para cliente {}", payload.clienteId());
        String clienteId = payload.clienteId(); // <-- Guardar o ID do cliente

        if (Boolean.TRUE.equals(payload.biometriaOk())) {
            try {
                // 1. Tenta validar o código recebido (pode lançar AutenticacaoIoTExpiradaException)
                Optional<CodigoAutenticacao> codigoValidado = autenticacaoIoTService
                        .validarCodigoRecebido(payload.clienteId(), payload.codigoValidado());

                if (codigoValidado.isPresent()) {
                    // 2. Se validou, busca qual era a transação que estava esperando por esse código
                    Optional<TransacaoPendente> pendenciaOpt = transacaoPendenteRepository
                            .findByCodigoAutenticacao(codigoValidado.get());

                    if (pendenciaOpt.isPresent()) {
                        TransacaoPendente pendencia = pendenciaOpt.get();
                        log.info(">>> EXECUTANDO OPERAÇÃO PENDENTE: {}", pendencia.getTipoOperacao());

                        try {
                            String mensagemSucesso = ""; // <-- Variável para guardar a msg
                            // 3. Executa a operação real baseada no tipo
                            switch (pendencia.getTipoOperacao()) {
                                case SAQUE -> {
                                    contaServiceDomain.sacar(
                                            pendencia.getContaOrigemNumero(), pendencia.getValor());
                                    mensagemSucesso = String.format("Saque de R$ %.2f concluído com sucesso.", pendencia.getValor());
                                }
                                case TRANSFERENCIA -> {
                                    contaServiceDomain.transferir(
                                            pendencia.getContaOrigemNumero(), pendencia.getContaDestinoNumero(), pendencia.getValor());
                                    mensagemSucesso = String.format("Transferência de R$ %.2f para conta %d concluída.",
                                            pendencia.getValor(), pendencia.getContaDestinoNumero());
                                }
                                case PAGAMENTO_BOLETO -> {
                                    // Recria o DTO necessário para o serviço de pagamento.
                                    // Nota: Estamos passando uma lista vazia de taxas aqui por simplificação.
                                    PagamentoRequestDTO dto = new PagamentoRequestDTO(
                                            pendencia.getCodigoBoleto(), pendencia.getValor(), Collections.emptyList());
                                    pagamentoAppService.realizarPagamento(pendencia.getContaOrigemNumero(), dto);
                                    mensagemSucesso = String.format("Pagamento de boleto no valor de R$ %.2f concluído.", pendencia.getValor());
                                }
                            }
                            // 4. SUCESSO: deleta a pendência (já foi processada)
                            transacaoPendenteRepository.delete(pendencia);
                            log.info(">>> OPERAÇÃO CONCLUÍDA COM SUCESSO!");
                            notificacaoService.enviarNotificacao(clienteId, NotificacaoDTO.sucesso(mensagemSucesso));

                        } catch (Exception e) {
                            // 5. ERRO DE NEGÓCIO (ex: Saldo insuficiente no momento da execução)
                            log.error(">>> ERRO AO EXECUTAR OPERAÇÃO PENDENTE: {}", e.getMessage(), e);
                            // Deletamos a pendência para não ficar travada no banco.
                            transacaoPendenteRepository.delete(pendencia);
                            notificacaoService.enviarNotificacao(clienteId, NotificacaoDTO.falha(e.getMessage()));
                        }
                    } else {
                        log.warn(">>> AVISO: Código validado, mas nenhuma transação pendente foi encontrada.");
                    }
                }
            } catch (AutenticacaoIoTExpiradaException e) {
                log.error(">>> ERRO IOT: {}", e.getMessage());
                notificacaoService.enviarNotificacao(clienteId, NotificacaoDTO.falha(e.getMessage()));
            }
        } else {
            log.warn(">>> FALHA BIOMETRIA: Cliente negou a operação no dispositivo.");
            notificacaoService.enviarNotificacao(clienteId, NotificacaoDTO.falha("Operação negada no dispositivo."));
        }
    }
}
