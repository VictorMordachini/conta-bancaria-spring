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

import java.util.Collections;
import java.util.Optional;

@Service
public class MqttListenerService {

    private final AutenticacaoIoTService autenticacaoIoTService;
    private final TransacaoPendenteRepository transacaoPendenteRepository;
    private final ContaServiceDomain contaServiceDomain;
    private final PagamentoAppService pagamentoAppService;

    public MqttListenerService(AutenticacaoIoTService autenticacaoIoTService,
                               TransacaoPendenteRepository transacaoPendenteRepository,
                               ContaServiceDomain contaServiceDomain,
                               PagamentoAppService pagamentoAppService) {
        this.autenticacaoIoTService = autenticacaoIoTService;
        this.transacaoPendenteRepository = transacaoPendenteRepository;
        this.contaServiceDomain = contaServiceDomain;
        this.pagamentoAppService = pagamentoAppService;
    }

    @MqttSubscriber("banco/validacao/+")
    @Transactional
    public void processarValidacaoIoT(@MqttPayload AutenticacaoValidacaoDTO payload) {
        System.out.println(">>> MQTT RECEBIDO: Validação para cliente " + payload.clienteId());

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
                        System.out.println(">>> EXECUTANDO OPERAÇÃO PENDENTE: " + pendencia.getTipoOperacao());

                        try {
                            // 3. Executa a operação real baseada no tipo
                            switch (pendencia.getTipoOperacao()) {
                                case SAQUE -> contaServiceDomain.sacar(
                                        pendencia.getContaOrigemNumero(), pendencia.getValor());
                                case TRANSFERENCIA -> contaServiceDomain.transferir(
                                        pendencia.getContaOrigemNumero(), pendencia.getContaDestinoNumero(), pendencia.getValor());
                                case PAGAMENTO_BOLETO -> {
                                    // Recria o DTO necessário para o serviço de pagamento.
                                    // Nota: Estamos passando uma lista vazia de taxas aqui por simplificação.
                                    PagamentoRequestDTO dto = new PagamentoRequestDTO(
                                            pendencia.getCodigoBoleto(), pendencia.getValor(), Collections.emptyList());
                                    pagamentoAppService.realizarPagamento(pendencia.getContaOrigemNumero(), dto);
                                }
                            }
                            // 4. SUCESSO: deleta a pendência (já foi processada)
                            transacaoPendenteRepository.delete(pendencia);
                            System.out.println(">>> OPERAÇÃO CONCLUÍDA COM SUCESSO!");

                        } catch (Exception e) {
                            // 5. ERRO DE NEGÓCIO (ex: Saldo insuficiente no momento da execução)
                            System.err.println(">>> ERRO AO EXECUTAR OPERAÇÃO PENDENTE: " + e.getMessage());
                            // Deletamos a pendência para não ficar travada no banco.
                            transacaoPendenteRepository.delete(pendencia);
                        }
                    } else {
                        System.err.println(">>> AVISO: Código validado, mas nenhuma transação pendente foi encontrada.");
                    }
                }
            } catch (AutenticacaoIoTExpiradaException e) {
                System.err.println(">>> ERRO IOT: " + e.getMessage());
            }
        } else {
            System.out.println(">>> FALHA BIOMETRIA: Cliente negou a operação no dispositivo.");
        }
    }
}
