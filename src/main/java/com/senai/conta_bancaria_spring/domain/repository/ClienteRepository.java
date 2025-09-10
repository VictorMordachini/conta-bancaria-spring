package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository <Cliente, String> {
}
