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

    public ClienteResponseDTO criarCliente(ClienteRequestDTO dto) {
        if (clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());

        Conta novaConta;
        Long numeroConta = (long) (265 + new Random().nextInt(90000)); // Gera um número de conta aleatório

        if ("Corrente".equalsIgnoreCase(dto.getTipoConta())) {
            novaConta = new ContaCorrente(numeroConta, dto.getSaldoInicial(), dto.getLimite(), dto.getTaxa());
        } else if ("Poupanca".equalsIgnoreCase(dto.getTipoConta())) {
            novaConta = new ContaPoupanca(numeroConta, dto.getSaldoInicial(), dto.getRendimento());
        } else {
            throw new IllegalArgumentException("Tipo de conta inválido. Use 'Corrente' ou 'Poupanca'.");
        }

        cliente.adicionarConta(novaConta);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        // REGISTRAR TRANSAÇÃO DE ABERTURA DE CONTA / DEPÓSITO INICIAL
        if (dto.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
            Transacao transacaoInicial = new Transacao();
            // Pega a referência da conta que acabou de ser salva junto com o cliente
            transacaoInicial.setConta(clienteSalvo.getContas().getFirst());
            transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
            transacaoInicial.setValor(dto.getSaldoInicial());
            transacaoRepository.save(transacaoInicial);
        }
        return ClienteResponseDTO.fromEntity(clienteSalvo);
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

    public ClienteResponseDTO atualizarCliente(String id, ClienteUpdateRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        // Atualiza apenas os campos permitidos, com base nos dados do DTO.
        cliente.setNome(dto.getNome());

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return ClienteResponseDTO.fromEntity(clienteAtualizado);
    }

    public void desativarCliente(String id) {
        // Busca o cliente pelo ID. Se não encontrar, lança exceção.
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        // Regra de negócio simples: apenas desativa.
        cliente.setAtivo(false);

        // Salva a alteração no banco de dados.
        clienteRepository.save(cliente);
    }

    public ContaResponseDTO abrirNovaConta(String clienteId, ContaRequestDTO dto) {

        // 1. Busca o cliente ou lança uma exceção se não encontrado.
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        // 2. Regra de Negócio: Verifica se o cliente já possui uma conta do tipo solicitado.
        boolean contaExistente = cliente.getContas().stream()
                .anyMatch(conta -> conta.getClass().getSimpleName().equalsIgnoreCase(dto.getTipoConta()));

        if (contaExistente) {
            throw new IllegalArgumentException("O cliente já possui uma conta do tipo " + dto.getTipoConta());
        }

        // 3. Cria a nova entidade Conta com base no DTO.
        Conta novaConta;
        long numeroConta = (265 + new Random().nextInt(90000));

        if ("Corrente".equalsIgnoreCase(dto.getTipoConta())) {
            novaConta = new ContaCorrente(numeroConta, dto.getSaldoInicial(), dto.getLimite(), dto.getTaxa());
        } else { // Já validamos que só pode ser "Poupanca"
            novaConta = new ContaPoupanca(numeroConta, dto.getSaldoInicial(), dto.getRendimento());
        }

        // 4. Associa a nova conta ao cliente e salva. A cascata fará o resto.
        cliente.adicionarConta(novaConta);
        clienteRepository.save(cliente);

        // 5. Registra a transação de abertura, se houver saldo inicial.
        if (dto.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
            Transacao transacaoInicial = new Transacao();
            transacaoInicial.setConta(novaConta);
            transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
            transacaoInicial.setValor(dto.getSaldoInicial());
            transacaoRepository.save(transacaoInicial);
        }

        // 6. Retorna um DTO da conta que acabou de ser criada.
        return ContaResponseDTO.fromEntity(novaConta);
    }
}
