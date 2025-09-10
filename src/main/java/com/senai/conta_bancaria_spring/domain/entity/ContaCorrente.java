package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ContaCorrente extends Conta{

    private Double limite;

    private Double taxa;
}
