package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.application.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(@Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO novoCliente = clienteService.criarCliente(clienteDTO);
        return new ResponseEntity<>(novoCliente, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable String id) {
        return ResponseEntity.ok(clienteService.buscarClientePorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable String id, @Valid @RequestBody ClienteUpdateRequestDTO dto) {
        ClienteResponseDTO clienteAtualizado = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(clienteAtualizado);
    }

    @PostMapping("/{clienteId}/contas")
    public ResponseEntity<ContaResponseDTO> abrirNovaConta(@PathVariable String clienteId, @Valid @RequestBody ContaRequestDTO dto) {
        ContaResponseDTO novaConta = clienteService.abrirNovaConta(clienteId, dto);

        // Retorna o status 201 Created com a nova conta no corpo da resposta.
        return new ResponseEntity<>(novaConta, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}") // Mapeia requisições HTTP DELETE para este método.
    @ResponseStatus(HttpStatus.NO_CONTENT) // Define o status de resposta padrão como 204 No Content.
    public void desativarCliente(@PathVariable String id) {
        clienteService.desativarCliente(id);
        // Para operações de delete/desativação, é comum não retornar nenhum corpo na resposta,
        // apenas o status 204, indicando que a operação foi bem-sucedida.
    }
}
