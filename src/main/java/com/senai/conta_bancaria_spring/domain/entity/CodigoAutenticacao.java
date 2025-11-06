package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_autenticacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoAutenticacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String codigo; // O código numérico (ex: "123456")

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Builder.Default
    @Column(nullable = false)
    private Boolean validado = false; // True apenas quando o dispositivo confirma

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
