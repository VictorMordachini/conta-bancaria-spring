package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.domain.entity.*;
import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import com.senai.conta_bancaria_spring.domain.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;

    public ClienteService(ClienteRepository clienteRepository, TransacaoRepository transacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public ClienteResponseDTO criarCliente(ClienteRequestDTO dto) {
        if (clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());

        Conta novaConta;
        long numeroConta = 100000L + new Random().nextInt(900000);

        if ("Corrente".equalsIgnoreCase(dto.getTipoConta())) {
            novaConta = ContaCorrente.builder()
                    .numero(numeroConta)
                    .saldo(dto.getSaldoInicial())
                    .limite(dto.getLimite())
                    .taxa(dto.getTaxa())
                    .ativa(true)
                    .build();
        } else {
            novaConta = ContaPoupanca.builder()
                    .numero(numeroConta)
                    .saldo(dto.getSaldoInicial())
                    .rendimento(dto.getRendimento())
                    .ativa(true)
                    .build();
        }

        cliente.adicionarConta(novaConta);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        if (dto.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
            Transacao transacaoInicial = new Transacao();
            transacaoInicial.setConta(clienteSalvo.getContas().get(0));
            transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
            transacaoInicial.setValor(dto.getSaldoInicial());
            transacaoRepository.save(transacaoInicial);
        }

        return ClienteResponseDTO.fromEntity(clienteSalvo);
    }

    @Transactional
    public ContaResponseDTO abrirNovaConta(String clienteId, ContaRequestDTO dto) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

            boolean contaExistente = cliente.getContas().stream()
                    .anyMatch(conta -> conta.getClass().getSimpleName().equalsIgnoreCase(dto.getTipoConta()));

            if (contaExistente) {
                throw new IllegalArgumentException("O cliente já possui uma conta do tipo " + dto.getTipoConta());
            }

            Conta novaConta;
            long numeroConta = 100000L + new Random().nextInt(900000);

            if ("Corrente".equalsIgnoreCase(dto.getTipoConta())) {
                novaConta = ContaCorrente.builder()
                        .numero(numeroConta)
                        .saldo(dto.getSaldoInicial())
                        .limite(dto.getLimite())
                        .taxa(dto.getTaxa())
                        .ativa(true)
                        .build();
            } else {
                novaConta = ContaPoupanca.builder()
                        .numero(numeroConta)
                        .saldo(dto.getSaldoInicial())
                        .rendimento(dto.getRendimento())
                        .ativa(true)
                        .build();
            }

            cliente.adicionarConta(novaConta);

            Cliente clienteSalvo = clienteRepository.save(cliente);

            Conta contaSalva = clienteSalvo.getContas().getLast();

            if (dto.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                Transacao transacaoInicial = new Transacao();
                transacaoInicial.setConta(contaSalva);
                transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
                transacaoInicial.setValor(dto.getSaldoInicial());
                transacaoRepository.save(transacaoInicial);
            }

            return ContaResponseDTO.fromEntity(contaSalva);
    }

    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAllByAtivoTrue().stream()
                .map(ClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO buscarClientePorId(String id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        return ClienteResponseDTO.fromEntity(cliente);
    }

    public void desativarCliente(String id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    public ClienteResponseDTO atualizarCliente(String id, ClienteUpdateRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        cliente.setNome(dto.getNome());
        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(clienteAtualizado);
    }
}