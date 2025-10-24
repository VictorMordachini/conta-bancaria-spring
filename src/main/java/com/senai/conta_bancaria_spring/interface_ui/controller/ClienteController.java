package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.application.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@Tag(name = "2. Clientes e Contas (Gerente & Público)", description = "Endpoints para gerenciamento de clientes e abertura de contas.")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Cria um novo cliente (Público)",
            description = "Registra um novo cliente e abre sua primeira conta. Endpoint público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(@Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO novoCliente = clienteService.criarCliente(clienteDTO);
        return new ResponseEntity<>(novoCliente, HttpStatus.CREATED);
    }

    @Operation(summary = "Lista todos os clientes ativos (GERENTE)",
            description = "Retorna uma lista de todos os clientes com status 'ativo'. Requer ROLE_GERENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes recuperada",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ClienteResponseDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @Operation(summary = "Busca um cliente por ID (GERENTE)",
            description = "Retorna os detalhes de um cliente específico pelo seu ID (UUID). Requer ROLE_GERENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable String id) {
        return ResponseEntity.ok(clienteService.buscarClientePorId(id));
    }

    @Operation(summary = "Atualiza o nome de um cliente (GERENTE)",
            description = "Atualiza os dados de um cliente (atualmente apenas o nome). Requer ROLE_GERENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: nome em branco)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable String id, @Valid @RequestBody ClienteUpdateRequestDTO dto) {
        ClienteResponseDTO clienteAtualizado = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(clienteAtualizado);
    }

    @Operation(summary = "Abre uma nova conta para um cliente (GERENTE)",
            description = "Adiciona uma nova conta (Corrente ou Poupança) a um cliente existente. Requer ROLE_GERENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta aberta com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ContaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou cliente já possui este tipo de conta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{clienteId}/contas")
    public ResponseEntity<ContaResponseDTO> abrirNovaConta(@PathVariable String clienteId, @Valid @RequestBody ContaRequestDTO dto) {
        ContaResponseDTO novaConta = clienteService.abrirNovaConta(clienteId, dto);

        // Retorna o status 201 Created com a nova conta no corpo da resposta.
        return new ResponseEntity<>(novaConta, HttpStatus.CREATED);
    }

    @Operation(summary = "Desativa um cliente (GERENTE)",
            description = "Realiza um 'soft delete' do cliente (define 'ativo' como false). Requer ROLE_GERENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente desativado com sucesso",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @DeleteMapping("/{id}") // Mapeia requisições HTTP DELETE para este método.
    @ResponseStatus(HttpStatus.NO_CONTENT) // Define o status de resposta padrão como 204 No Content.
    public void desativarCliente(@PathVariable String id) {
        clienteService.desativarCliente(id);
        // Para operações de delete/desativação, é comum não retornar nenhum corpo na resposta,
        // apenas o status 204, indicando que a operação foi bem-sucedida.
    }
}
