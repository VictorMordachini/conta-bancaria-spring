package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.application.service.AutenticacaoIoTService;
import com.senai.conta_bancaria_spring.application.service.PagamentoAppService;
import com.senai.conta_bancaria_spring.application.service.TransacaoPendenteService;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
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
    private final AutenticacaoIoTService autenticacaoIoTService;
    private final TransacaoPendenteService transacaoPendenteService;

    public ContaController(ContaServiceDomain contaService,
                           PagamentoAppService pagamentoAppService,
                           AutenticacaoIoTService autenticacaoIoTService,
                           TransacaoPendenteService transacaoPendenteService) {
        this.contaService = contaService;
        this.pagamentoAppService = pagamentoAppService;
        this.autenticacaoIoTService = autenticacaoIoTService;
        this.transacaoPendenteService = transacaoPendenteService;
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

    @Operation(summary = "Solicita um saque (CLIENTE)",
            description = "Inicia uma solicitação de saque. Requer confirmação biométrica via IoT para ser concluído.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita. Aguardando confirmação IoT.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"mensagem\": \"Operação de Saque iniciada. Aguarde a confirmação biométrica no seu dispositivo.\"}"))),
            @ApiResponse(responseCode = "400", description = "Valor inválido (ex: negativo)",
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
        // 1. Busca a conta apenas para identificar o cliente (sem travar linha no banco ainda)
        Conta conta = contaService.buscarPorNumero(numeroConta);

        // 2. Inicia o processo de autenticação IoT
        String idCodigo = autenticacaoIoTService.solicitarAutenticacao(conta.getCliente());

        // 3. Salva a intenção de saque como pendente
        transacaoPendenteService.criarSaquePendente(numeroConta, dto.valor(), idCodigo);

        // 4. Retorna 202 Accepted para o cliente da API
        return ResponseEntity.accepted()
                .body(Map.of("mensagem", "Operação de Saque iniciada. Aguarde a confirmação biométrica no seu dispositivo."));
    }

    @Operation(summary = "Solicita uma transferência (CLIENTE)",
            description = "Inicia uma solicitação de transferência. Requer confirmação biométrica via IoT para ser concluída.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita. Aguardando confirmação IoT.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"mensagem\": \"Transferência iniciada. Aguarde a confirmação biométrica.\"}"))),
            // ... (erros 400, 403, 404)
    })
    @PostMapping("/{numeroContaOrigem}/transferir")
    public ResponseEntity<Map<String, String>> transferir(@PathVariable Long numeroContaOrigem, @Valid @RequestBody TransferenciaRequestDTO dto) {
        Conta conta = contaService.buscarPorNumero(numeroContaOrigem);
        String idCodigo = autenticacaoIoTService.solicitarAutenticacao(conta.getCliente());
        transacaoPendenteService.criarTransferenciaPendente(numeroContaOrigem, dto, idCodigo);

        return ResponseEntity.accepted()
                .body(Map.of("mensagem", "Transferência iniciada. Aguarde a confirmação biométrica."));
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

    @Operation(summary = "Solicita um pagamento de boleto (CLIENTE)",
            description = "Inicia uma solicitação de pagamento. Requer confirmação biométrica via IoT para ser concluído.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita. Aguardando confirmação IoT.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"mensagem\": \"Pagamento iniciado. Aguarde a confirmação biométrica.\"}"))),
            // ... (erros 400, 403, 404)
    })
    @PostMapping("/{numeroConta}/pagar")
    public ResponseEntity<Map<String, String>> realizarPagamento(@PathVariable Long numeroConta, @Valid @RequestBody PagamentoRequestDTO dto) {
        Conta conta = contaService.buscarPorNumero(numeroConta);
        String idCodigo = autenticacaoIoTService.solicitarAutenticacao(conta.getCliente());
        transacaoPendenteService.criarPagamentoPendente(numeroConta, dto, idCodigo);

        // Nota: Mudamos o retorno de PagamentoResponseDTO para Map, pois a resposta final não existe ainda.
        return ResponseEntity.accepted()
                .body(Map.of("mensagem", "Pagamento iniciado. Aguarde a confirmação biométrica."));
    }

}
