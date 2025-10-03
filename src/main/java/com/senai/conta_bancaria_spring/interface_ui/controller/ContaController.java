package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.*;
import com.senai.conta_bancaria_spring.domain.service.ContaServiceDomain;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contas")
public class ContaController {
    private final ContaServiceDomain contaService;


    public ContaController(ContaServiceDomain contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/{numeroConta}/depositar")
    public ResponseEntity<Map<String, String>> depositar(@PathVariable Long numeroConta, @Valid @RequestBody OperacaoRequestDTO dto) {
        contaService.depositar(numeroConta, dto.getValor());
        return ResponseEntity.ok(Map.of("mensagem", "Depósito realizado com sucesso."));
    }

    @PostMapping("/{numeroConta}/sacar")
    public ResponseEntity<Map<String, String>> sacar(@PathVariable Long numeroConta, @Valid @RequestBody OperacaoRequestDTO dto) {
        contaService.sacar(numeroConta, dto.getValor());
        return ResponseEntity.ok(Map.of("mensagem", "Saque realizado com sucesso."));
    }

    @PostMapping("/{numeroContaOrigem}/transferir")
    public ResponseEntity<Map<String, String>> transferir(@PathVariable Long numeroContaOrigem, @Valid @RequestBody TransferenciaRequestDTO dto) {
        contaService.transferir(numeroContaOrigem, dto.getNumeroContaDestino(), dto.getValor());
        return ResponseEntity.ok(Map.of("mensagem", "Transferência realizada com sucesso."));
    }

    // ENDPOINT PARA O EXTRATO
    @GetMapping("/{numeroConta}/extrato")
    public ResponseEntity<List<TransacaoResponseDTO>> buscarExtrato(@PathVariable Long numeroConta) {
        List<TransacaoResponseDTO> extrato = contaService.buscarExtratoPorNumeroConta(numeroConta);
        return ResponseEntity.ok(extrato);
    }

    @PatchMapping("/corrente/{numeroConta}")
    public ResponseEntity<ContaResponseDTO> atualizarParametrosContaCorrente(
            @PathVariable Long numeroConta,
            @Valid @RequestBody ContaCorrenteUpdateRequestDTO dto) {

        ContaResponseDTO contaAtualizada = contaService.atualizarParametrosContaCorrente(numeroConta, dto);
        return ResponseEntity.ok(contaAtualizada);
    }


}
