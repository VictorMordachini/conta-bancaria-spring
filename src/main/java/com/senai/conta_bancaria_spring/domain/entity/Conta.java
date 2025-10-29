package com.senai.conta_bancaria_spring.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senai.conta_bancaria_spring.domain.exception.SaldoInsuficienteException;
import com.senai.conta_bancaria_spring.domain.exception.ValorInvalidoException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "contas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conta_numero", columnNames = "numero"),
                @UniqueConstraint(name = "uk_conta_cliente", columnNames = {"cliente_id", "tipo_conta"})
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_conta", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private Long numero;

    @Column(nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO; // VALOR PADRÃO: Garante que nunca seja nulo.

    @Column(nullable = false)
    private Boolean ativa = true; // VALOR PADRÃO: Garante que nunca seja nulo.

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name = "fk_conta_cliente"))
    @JsonIgnore
    private Cliente cliente;

    public abstract void sacar(BigDecimal valor);

    public void depositar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.TEN) <= 0) {
            throw new ValorInvalidoException("O valor do depósito deve ser maior que R$10,00.");
        }
        this.saldo = this.saldo.add(valor);
    }

    protected void validarValorDebitoPositivo(BigDecimal valor, String tipoOperacao) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            // Usamos String.format para criar uma mensagem de erro mais dinâmica.
            throw new ValorInvalidoException(String.format("O valor do %s deve ser maior que R$0,00.", tipoOperacao));
        }
    }

    protected void validarSaldoSuficiente(BigDecimal valor) {
        if (valor.compareTo(this.getSaldo()) > 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente.");
        }
    }

    protected void validarSaldoComLimiteSuficiente(BigDecimal valor, Long limite) {
        BigDecimal saldoDisponivel = this.getSaldo().add(BigDecimal.valueOf(limite));
        if (valor.compareTo(saldoDisponivel) > 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente, mesmo com o limite.");
        }
    }

    public abstract BigDecimal debitarParaTransferencia(BigDecimal valor);
}