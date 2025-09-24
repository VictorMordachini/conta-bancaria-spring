package com.senai.conta_bancaria_spring.domain.entity;

import com.senai.conta_bancaria_spring.domain.enums.TipoTransacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
@Getter
@Setter
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora; // Registra a data e hora exatas da transação.

    @Enumerated(EnumType.STRING) // Grava o nome do enum (ex: "DEPOSITO") no banco em vez de um número.
    @Column(nullable = false, updatable = false)
    private TipoTransacao tipo;

    @Column(nullable = false, updatable = false)
    private BigDecimal valor;

    // Relacionamento com a conta principal envolvida na transação.
    // Muitas transações podem estar associadas a uma conta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transacao_conta"))
    private Conta conta;

    // Campo opcional para registrar a conta de destino em uma transferência.
    @Column(name = "conta_destino_numero")
    private Long contaDestinoNumero;

    // Método que é executado antes da entidade ser salva pela primeira vez.
    // Usamos isso para garantir que a data e hora sejam sempre definidas.
    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now();
    }
}
