package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.TaxaRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.TaxaResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.exception.RecursoNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.TaxaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaxaService {
    private final TaxaRepository taxaRepository;

    public TaxaService(TaxaRepository taxaRepository) {
        this.taxaRepository = taxaRepository;
    }

    public TaxaResponseDTO criarTaxa(TaxaRequestDTO dto) {
        Taxa novaTaxa = Taxa.builder()
                .descricao(dto.descricao())
                .percentual(dto.percentual())
                .valorFixo(dto.valorFixo())
                .build();

        Taxa taxaSalva = taxaRepository.save(novaTaxa);
        return TaxaResponseDTO.fromEntity(taxaSalva);
    }

    public TaxaResponseDTO buscarPorId(String id) {
        Taxa taxa = buscarTaxaOuFalhar(id);
        return TaxaResponseDTO.fromEntity(taxa);
    }

    public List<TaxaResponseDTO> listarTodas() {
        return taxaRepository.findAll().stream()
                .map(TaxaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public TaxaResponseDTO atualizarTaxa(String id, TaxaRequestDTO dto) {
        Taxa taxaExistente = buscarTaxaOuFalhar(id);

        taxaExistente.setDescricao(dto.descricao());
        taxaExistente.setPercentual(dto.percentual());
        taxaExistente.setValorFixo(dto.valorFixo());

        Taxa taxaAtualizada = taxaRepository.save(taxaExistente);
        return TaxaResponseDTO.fromEntity(taxaAtualizada);
    }

    public void deletarTaxa(String id) {
        Taxa taxa = buscarTaxaOuFalhar(id);
        taxaRepository.delete(taxa);
    }

    // Método utilitário privado para evitar repetição de código
    private Taxa buscarTaxaOuFalhar(String id) {
        return taxaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Taxa não encontrada com o ID: " + id));
    }
}
