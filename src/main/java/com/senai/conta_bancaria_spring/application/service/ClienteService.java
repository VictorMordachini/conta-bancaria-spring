package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.ClienteRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.ClienteResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.entity.ContaPoupanca;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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
        return ClienteResponseDTO.fromEntity(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(ClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO buscarClientePorId(String id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        return ClienteResponseDTO.fromEntity(cliente);
    }

}
