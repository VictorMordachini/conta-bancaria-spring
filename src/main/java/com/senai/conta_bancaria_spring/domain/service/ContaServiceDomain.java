package com.senai.conta_bancaria_spring.domain.service;

import com.senai.conta_bancaria_spring.application.dto.ContaCorrenteUpdateRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.ContaResponseDTO;
import com.senai.conta_bancaria_spring.application.dto.TransacaoResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.entity.Transacao;
import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;
import com.senai.conta_bancaria_spring.domain.exception.RecursoNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import com.senai.conta_bancaria_spring.domain.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class ContaServiceDomain {
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaServiceDomain(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public Conta buscarPorNumero(Long numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o número: " + numero));
    }

    public void depositar(Long numeroConta, BigDecimal valor) {
        Conta conta = buscarPorNumero(numeroConta);
        conta.depositar(valor);
        contaRepository.save(conta);

        registrarTransacao(conta, TipoTransacao.DEPOSITO, valor, null);
    }

    public void sacar(Long numeroConta, BigDecimal valor) {
        Conta conta = buscarPorNumero(numeroConta);
        //O Java chama o método sacar() da classe concreta (ContaCorrente ou ContaPoupanca).
        conta.sacar(valor);
        contaRepository.save(conta);

        registrarTransacao(conta, TipoTransacao.SAQUE, valor.negate(), null); // Valor negativo para representar saída
    }

    @Transactional
    public void transferir(Long numeroContaOrigem, Long numeroContaDestino, BigDecimal valor) {
        if (numeroContaOrigem.equals(numeroContaDestino)) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser as mesmas.");
        }

        Conta contaOrigem = buscarPorNumero(numeroContaOrigem);
        Conta contaDestino = buscarPorNumero(numeroContaDestino);

        // 1. Delega a lógica de débito para a própria entidade (Polimorfismo!).
        BigDecimal valorDebitado = contaOrigem.debitarParaTransferencia(valor);

        // 2. Reutilizamos o método depositar para creditar na conta de destino.
        contaDestino.depositar(valor);

        // 3. Salvamos o estado atualizado das duas contas.
        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        registrarTransacao(contaOrigem, TipoTransacao.TRANSFERENCIA_ENVIADA, valorDebitado.negate(), numeroContaDestino);
        registrarTransacao(contaDestino, TipoTransacao.TRANSFERENCIA_RECEBIDA, valor, numeroContaOrigem);
    }

    // MÉTODO PARA BUSCAR O EXTRATO
    public List<TransacaoResponseDTO> buscarExtratoPorNumeroConta(Long numeroConta) {
        // Primeiro, verifica se a conta existe. Se não, o método buscarPorNumero já lança a exceção.
        buscarPorNumero(numeroConta);

        // Busca a lista de entidades Transacao do banco de dados, já ordenadas.
        List<Transacao> transacoes = transacaoRepository.findByContaNumeroOrderByDataHoraDesc(numeroConta);

        // Converte a lista de entidades para uma lista de DTOs e a retorna.
        return transacoes.stream()
                .map(TransacaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    //MÉTODO AUXILIAR PARA EVITAR REPETIÇÃO DE CÓDIGO
    private void registrarTransacao(Conta conta, TipoTransacao tipo, BigDecimal valor, Long contaDestinoNumero) {
        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setTipo(tipo);
        transacao.setValor(valor);
        transacao.setContaDestinoNumero(contaDestinoNumero);
        transacaoRepository.save(transacao);
    }

    public ContaResponseDTO atualizarParametrosContaCorrente(Long numeroConta, ContaCorrenteUpdateRequestDTO dto) {
        // 1. Busca a conta
        Conta conta = buscarPorNumero(numeroConta);

        // 2. Valida se é realmente uma Conta Corrente
        if (!(conta instanceof ContaCorrente contaCorrente)) {
            throw new IllegalArgumentException("Apenas contas do tipo Corrente podem ser atualizadas.");
        }

        // 3. "Diz" para a entidade se atualizar. Não pergunta sobre seus campos.
        contaCorrente.atualizarParametros(dto);

        // 4. Salva e retorna a conta atualizada
        Conta contaAtualizada = contaRepository.save(contaCorrente);
        return ContaResponseDTO.fromEntity(contaAtualizada);
    }


}
