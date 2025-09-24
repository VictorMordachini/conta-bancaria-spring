package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository <Transacao, String> {
    // Query Method para buscar todas as transações de uma conta específica,
    // ordenadas pela data e hora em ordem decrescente (as mais recentes primeiro).
    List<Transacao> findByContaNumeroOrderByDataHoraDesc(Long numeroConta);
}
