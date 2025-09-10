package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.ContaPoupanca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaPoupancaRepository extends JpaRepository <ContaPoupanca, String> {
}
