package com.senai.conta_bancaria_spring.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senai.conta_bancaria_spring.domain.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // [cite: 7]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_conta"))
    @JsonIgnore
    private Conta conta; //

    @Column(nullable = false, updatable = false)
    private String boleto; // [cite: 9]

    @Column(nullable = false, updatable = false)
    private BigDecimal valorPago; // [cite: 10]

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataPagamento; //

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status; //

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER) // Eager para trazer as taxas junto com o pagamento
    @JoinTable(
            name = "pagamento_taxas",
            joinColumns = @JoinColumn(name = "pagamento_id"),
            inverseJoinColumns = @JoinColumn(name = "taxa_id")
    )
    private Set<Taxa> taxas = new HashSet<>(); //

    // Similar à entidade Transacao, seta a data e hora no momento da criação
    @PrePersist
    protected void onCreate() {
        this.dataPagamento = LocalDateTime.now();
    }
}
