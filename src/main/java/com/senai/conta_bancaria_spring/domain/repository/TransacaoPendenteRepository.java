package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.entity.TransacaoPendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransacaoPendenteRepository extends JpaRepository<TransacaoPendente, String> {
    Optional<TransacaoPendente> findByCodigoAutenticacao(CodigoAutenticacao codigoAutenticacao);
    void deleteByDataCriacaoBefore(LocalDateTime dataLimite);
}
