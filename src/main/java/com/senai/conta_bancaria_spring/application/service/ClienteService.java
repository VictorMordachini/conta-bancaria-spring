package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.config.BancoConfigProperties;
import com.senai.conta_bancaria_spring.domain.entity.*;
import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;
import com.senai.conta_bancaria_spring.domain.enums.UserRole;
import com.senai.conta_bancaria_spring.domain.exception.RecursoNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.exception.RegraDeNegocioException;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import com.senai.conta_bancaria_spring.domain.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final BancoConfigProperties bancoConfig;
    private final PasswordEncoder passwordEncoder;

    @Value("${banco.conta-poupanca.rendimento-padrao}") // Injeta o valor da propriedade diretamente neste campo.
    private BigDecimal rendimentoPadrao;

    public ClienteService(ClienteRepository clienteRepository, TransacaoRepository transacaoRepository, BancoConfigProperties bancoConfigProperties, BancoConfigProperties bancoConfig, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
        this.bancoConfig = bancoConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteResponseDTO criarCliente(ClienteRequestDTO dto) {
        if (clienteRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new RegraDeNegocioException("CPF já cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setSenha(passwordEncoder.encode(dto.senha()));
        cliente.setRole(UserRole.CLIENTE);

        Conta novaConta = criarInstanciaDeConta(dto.tipoConta(), dto.saldoInicial(), dto.limite(), dto.taxa(), dto.rendimento());

        cliente.adicionarConta(novaConta);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        if (dto.saldoInicial().compareTo(BigDecimal.ZERO) > 0) {
            Transacao transacaoInicial = new Transacao();
            transacaoInicial.setConta(clienteSalvo.getContas().getFirst());
            transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
            transacaoInicial.setValor(dto.saldoInicial());
            transacaoRepository.save(transacaoInicial);
        }

        return ClienteResponseDTO.fromEntity(clienteSalvo);
    }

    @Transactional
    public ContaResponseDTO abrirNovaConta(String clienteId, ContaRequestDTO dto) {
        Cliente cliente = buscarClientePorIdOuFalhar(clienteId);

        boolean contaExistente = cliente.getContas().stream()
                .anyMatch(conta -> conta.getClass().getSimpleName().equalsIgnoreCase(dto.tipoConta()));

        if (contaExistente) {
            throw new RegraDeNegocioException("O cliente já possui uma conta do tipo " + dto.tipoConta());
        }

        Conta novaConta = criarInstanciaDeConta(dto.tipoConta(), dto.saldoInicial(), dto.limite(), dto.taxa(), dto.rendimento());

        cliente.adicionarConta(novaConta);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        Conta contaSalva = clienteSalvo.getContas().getLast();

        if (dto.saldoInicial().compareTo(BigDecimal.ZERO) > 0) {
            Transacao transacaoInicial = new Transacao();
            transacaoInicial.setConta(contaSalva);
            transacaoInicial.setTipo(TipoTransacao.ABERTURA_CONTA);
            transacaoInicial.setValor(dto.saldoInicial());
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
        Cliente cliente = buscarClientePorIdOuFalhar(id);
        return ClienteResponseDTO.fromEntity(cliente);
    }

    public void desativarCliente(String id) {
        Cliente cliente = buscarClientePorIdOuFalhar(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    public ClienteResponseDTO atualizarCliente(String id, ClienteUpdateRequestDTO dto) {
        Cliente cliente = buscarClientePorIdOuFalhar(id);
        cliente.setNome(dto.nome());
        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(clienteAtualizado);
    }

    private Conta criarInstanciaDeConta(String tipoConta, BigDecimal saldoInicial, Long limite, BigDecimal taxa, BigDecimal rendimento) {
        long numeroConta = 100000L + new Random().nextInt(900000);

        if ("Corrente".equalsIgnoreCase(tipoConta)) {
            // Se o limite do DTO for nulo, usa o padrão. Senão, usa o do DTO.
            Long limiteFinal = (limite != null) ? limite : bancoConfig.getLimitePadrao();
            // Se a taxa do DTO for nula, usa a padrão. Senão, usa a do DTO.
            BigDecimal taxaFinal = (taxa != null) ? taxa : bancoConfig.getTaxaPadrao();

            return ContaCorrente.builder()
                    .numero(numeroConta)
                    .saldo(saldoInicial)
                    .limite(limiteFinal) // Usa o valor final
                    .taxa(taxaFinal)     // Usa o valor final
                    .ativa(true)
                    .build();
        } else {
            BigDecimal rendimentoFinal = (rendimento != null) ? rendimento : this.rendimentoPadrao;

            return ContaPoupanca.builder()
                    .numero(numeroConta)
                    .saldo(saldoInicial)
                    .rendimento(rendimentoFinal)
                    .ativa(true)
                    .build();
        }
    }

    private Cliente buscarClientePorIdOuFalhar(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o ID: " + id));
    }
}