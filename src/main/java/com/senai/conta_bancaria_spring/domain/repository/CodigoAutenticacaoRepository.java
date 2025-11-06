package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoAutenticacaoRepository extends JpaRepository<CodigoAutenticacao, String> {
    // Busca o último código aberto para o cliente
    Optional<CodigoAutenticacao> findFirstByClienteIdAndValidadoFalseOrderByExpiraEmDesc(String clienteId);
}
