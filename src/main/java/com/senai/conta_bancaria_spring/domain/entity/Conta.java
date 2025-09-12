package com.senai.conta_bancaria_spring.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "contas")
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Estratégia de tabela única
@DiscriminatorColumn(name = "tipo_conta", discriminatorType = DiscriminatorType.STRING) // Coluna que diferencia os tipos
public abstract class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private Long numero;

    private Double saldo = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnore // Evita serialização infinita
    private Cliente cliente;

    public Conta(Long numero, Double saldo) {
        this.numero = numero;
        this.saldo = saldo;
    }

    // Métodos de negócio que podem ser sobrescritos
    public abstract void sacar(Double valor);

    public void depositar(Double valor) {
        if (valor <= 10.00) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que R$10,00.");
        }
        this.saldo += valor;
    }
}
