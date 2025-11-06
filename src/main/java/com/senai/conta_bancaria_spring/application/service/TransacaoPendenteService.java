package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.PagamentoRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.TransferenciaRequestDTO;
import com.senai.conta_bancaria_spring.domain.entity.*;
import com.senai.conta_bancaria_spring.domain.enums.TipoOperacao;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import com.senai.conta_bancaria_spring.domain.repository.TransacaoPendenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransacaoPendenteService {
    private final TransacaoPendenteRepository transacaoPendenteRepository;
    private final CodigoAutenticacaoRepository codigoAutenticacaoRepository;

    public TransacaoPendenteService(TransacaoPendenteRepository transacaoPendenteRepository, CodigoAutenticacaoRepository codigoAutenticacaoRepository) {
        this.transacaoPendenteRepository = transacaoPendenteRepository;
        this.codigoAutenticacaoRepository = codigoAutenticacaoRepository;
    }

    @Transactional
    public void criarSaquePendente(Long contaOrigem, BigDecimal valor, String idCodigoAutenticacao) {
        salvarPendencia(TipoOperacao.SAQUE, contaOrigem, null, valor, null, idCodigoAutenticacao);
    }

    @Transactional
    public void criarTransferenciaPendente(Long contaOrigem, TransferenciaRequestDTO dto, String idCodigoAutenticacao) {
        salvarPendencia(TipoOperacao.TRANSFERENCIA, contaOrigem, dto.numeroContaDestino(), dto.valor(), null, idCodigoAutenticacao);
    }

    @Transactional
    public void criarPagamentoPendente(Long contaOrigem, PagamentoRequestDTO dto, String idCodigoAutenticacao) {
        // Nota: Para simplificar, não estamos salvando as taxas selecionadas na pendência.
        // Em um sistema real, precisaríamos de uma tabela auxiliar para isso.
        salvarPendencia(TipoOperacao.PAGAMENTO_BOLETO, contaOrigem, null, dto.valor(), dto.codigoBoleto(), idCodigoAutenticacao);
    }

    private void salvarPendencia(TipoOperacao tipo, Long origem, Long destino, BigDecimal valor, String boleto, String idCodigo) {
        CodigoAutenticacao codigoAuth = codigoAutenticacaoRepository.findById(idCodigo)
                .orElseThrow(() -> new IllegalStateException("Código de autenticação não encontrado ao criar pendência."));

        TransacaoPendente pendente = TransacaoPendente.builder()
                .tipoOperacao(tipo)
                .contaOrigemNumero(origem)
                .contaDestinoNumero(destino)
                .valor(valor)
                .codigoBoleto(boleto)
                .codigoAutenticacao(codigoAuth)
                .build();

        transacaoPendenteRepository.save(pendente);
    }
}
