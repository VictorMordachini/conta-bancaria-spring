package com.senai.conta_bancaria_spring.domain.entity;

import com.senai.conta_bancaria_spring.domain.enums.TipoOperacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes_pendentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoPendente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOperacao tipoOperacao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private Long contaOrigemNumero;

    @Column(nullable = true)
    private Long contaDestinoNumero; // Apenas para Transferência

    @Column(nullable = true)
    private String codigoBoleto;     // Apenas para Pagamento

    // Relacionamento crucial: vincula esta pendência ao código 2FA que estamos esperando
    @OneToOne
    @JoinColumn(name = "codigo_autenticacao_id", nullable = false, unique = true)
    private CodigoAutenticacao codigoAutenticacao;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }
}
