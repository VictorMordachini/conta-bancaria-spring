package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dispositivos_iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoIoT {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String codigoSerial; // Serial único do dispositivo físico

    @Column(nullable = false)
    private String chavePublica; // Para (futura) validação de assinatura digital

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
}
