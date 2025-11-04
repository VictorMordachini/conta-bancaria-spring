package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.TaxaRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.TaxaResponseDTO;
import com.senai.conta_bancaria_spring.application.service.TaxaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taxas")
@Tag(name = "4. Gerenciamento de Taxas (GERENTE)", description = "Endpoints para administradores criarem e gerenciarem taxas.")
public class TaxaController {
    private final TaxaService taxaService;

    public TaxaController(TaxaService taxaService) {
        this.taxaService = taxaService;
    }

    @Operation(summary = "Cria uma nova taxa (GERENTE)", description = "Registra uma nova taxa de operação no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Taxa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)")
    })
    @PostMapping
    public ResponseEntity<TaxaResponseDTO> criarTaxa(@Valid @RequestBody TaxaRequestDTO dto) {
        TaxaResponseDTO novaTaxa = taxaService.criarTaxa(dto);
        return new ResponseEntity<>(novaTaxa, HttpStatus.CREATED);
    }

    @Operation(summary = "Lista todas as taxas (GERENTE)", description = "Retorna uma lista de todas as taxas cadastradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de taxas recuperada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)")
    })
    @GetMapping
    public ResponseEntity<List<TaxaResponseDTO>> listarTodasTaxas() {
        return ResponseEntity.ok(taxaService.listarTodas());
    }

    @Operation(summary = "Busca uma taxa por ID (GERENTE)", description = "Retorna os detalhes de uma taxa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)"),
            @ApiResponse(responseCode = "404", description = "Taxa não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaxaResponseDTO> buscarTaxaPorId(@PathVariable String id) {
        return ResponseEntity.ok(taxaService.buscarPorId(id));
    }

    @Operation(summary = "Atualiza uma taxa (GERENTE)", description = "Atualiza os dados de uma taxa existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)"),
            @ApiResponse(responseCode = "404", description = "Taxa não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaxaResponseDTO> atualizarTaxa(@PathVariable String id, @Valid @RequestBody TaxaRequestDTO dto) {
        TaxaResponseDTO taxaAtualizada = taxaService.atualizarTaxa(id, dto);
        return ResponseEntity.ok(taxaAtualizada);
    }

    @Operation(summary = "Deleta uma taxa (GERENTE)", description = "Remove uma taxa do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Taxa deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (requer ROLE_GERENTE)"),
            @ApiResponse(responseCode = "404", description = "Taxa não encontrada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarTaxa(@PathVariable String id) {
        taxaService.deletarTaxa(id);
    }
}
