package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.application.service.PagamentoAppService;
import com.senai.conta_bancaria_spring.domain.service.ContaServiceDomain;
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
@RequestMapping("/contas")
@Tag(name = "3. Operações de Conta (Cliente)", description = "Endpoints para operações financeiras. Requer ROLE_CLIENTE e posse da conta.")
public class ContaController {
    private final ContaServiceDomain contaService;
    private final PagamentoAppService pagamentoAppService;


    public ContaController(ContaServiceDomain contaService, PagamentoAppService pagamentoAppService) {
        this.contaService = contaService;
        this.pagamentoAppService = pagamentoAppService;
    }

    @Operation(summary = "Realiza um depósito (CLIENTE)",
            description = "Adiciona um valor ao saldo da conta. Requer ROLE_CLIENTE e ser o proprietário da conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Valor de depósito inválido (ex: menor que R$10,00)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{numeroConta}/depositar")
    public ResponseEntity<Map<String, String>> depositar(@PathVariable Long numeroConta, @Valid @RequestBody OperacaoRequestDTO dto) {
        contaService.depositar(numeroConta, dto.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Depósito realizado com sucesso."));
    }

    @Operation(summary = "Realiza um saque (CLIENTE)",
            description = "Retira um valor do saldo da conta (aplicando taxas, se for Conta Corrente). Requer ROLE_CLIENTE e ser o proprietário da conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido ou saldo insuficiente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{numeroConta}/sacar")
    public ResponseEntity<Map<String, String>> sacar(@PathVariable Long numeroConta, @Valid @RequestBody OperacaoRequestDTO dto) {
        contaService.sacar(numeroConta, dto.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Saque realizado com sucesso."));
    }

    @Operation(summary = "Realiza uma transferência (CLIENTE)",
            description = "Transfere um valor de uma conta de origem para uma de destino. Requer ROLE_CLIENTE e ser o proprietário da conta de *origem*.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, saldo insuficiente ou contas iguais",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta de origem)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta de origem ou destino não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{numeroContaOrigem}/transferir")
    public ResponseEntity<Map<String, String>> transferir(@PathVariable Long numeroContaOrigem, @Valid @RequestBody TransferenciaRequestDTO dto) {
        contaService.transferir(numeroContaOrigem, dto.numeroContaDestino(), dto.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Transferência realizada com sucesso."));
    }

    // ENDPOINT PARA O EXTRATO
    @Operation(summary = "Busca o extrato da conta (CLIENTE)",
            description = "Retorna a lista de transações da conta, ordenadas da mais recente para a mais antiga. Requer ROLE_CLIENTE e ser o proprietário da conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato recuperado com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransacaoResponseDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{numeroConta}/extrato")
    public ResponseEntity<List<TransacaoResponseDTO>> buscarExtrato(@PathVariable Long numeroConta) {
        List<TransacaoResponseDTO> extrato = contaService.buscarExtratoPorNumeroConta(numeroConta);
        return ResponseEntity.ok(extrato);
    }

    @Operation(summary = "Atualiza parâmetros da Conta Corrente (CLIENTE)",
            description = "Permite ao proprietário da conta atualizar o limite e/ou taxa (parcialmente). Requer ROLE_CLIENTE e ser o proprietário da conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta corrente atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ContaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou a conta não é Corrente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PatchMapping("/corrente/{numeroConta}")
    public ResponseEntity<ContaResponseDTO> atualizarParametrosContaCorrente(
            @PathVariable Long numeroConta,
            @Valid @RequestBody ContaCorrenteUpdateRequestDTO dto) {

        ContaResponseDTO contaAtualizada = contaService.atualizarParametrosContaCorrente(numeroConta, dto);
        return ResponseEntity.ok(contaAtualizada);
    }

    @Operation(summary = "Realiza um pagamento de boleto (CLIENTE)",
            description = "Debita um valor da conta para pagar um boleto, aplicando taxas selecionadas. " +
                    "Registra a operação mesmo em caso de falha (ex: saldo insuficiente).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, boleto vencido ou saldo insuficiente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é o proprietário da conta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Conta ou Taxa(s) não encontrada(s)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{numeroConta}/pagar")
    public ResponseEntity<PagamentoResponseDTO> realizarPagamento(
            @PathVariable Long numeroConta,
            @Valid @RequestBody PagamentoRequestDTO dto) {

        PagamentoResponseDTO response = pagamentoAppService.realizarPagamento(numeroConta, dto);

        // Retorna 201 Created (pois um recurso 'Pagamento' foi criado com SUCESSO)
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
