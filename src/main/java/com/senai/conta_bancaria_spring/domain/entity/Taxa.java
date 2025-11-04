package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "taxas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taxa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String descricao; // Ex: IOF, Tarifa Bancária [cite: 16]

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal percentual = BigDecimal.ZERO; // Taxa percentual sobre o valor [cite: 17]

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal valorFixo = BigDecimal.ZERO; // Valor fixo adicional [cite: 18]
}
