package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByCpf(Long cpf);
    // Query Method para buscar todos os clientes onde o campo 'ativo' é true.
    List<Cliente> findAllByAtivoTrue();
}
