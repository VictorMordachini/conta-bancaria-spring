package com.senai.conta_bancaria_spring.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DialectOverride;

import java.math.BigDecimal;

@Entity
//Permite especificar detalhes da tabela que será criada para esta entidade
@Table(name = "contas",
        //Define restrições de unicidade a nível de tabela
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conta_numero", columnNames = "numero"),
                @UniqueConstraint(name = "uk_conta_cliente", columnNames = {"cliente_id", "tipo_conta"})
        }
)
@SuperBuilder // Ferramenta para criar objetos de forma fluida e legível
@Data //Um "pacotão" que gera os métodos mais comuns (getters, setters, toString, etc.)

//Implementam o padrão de Herança no banco de dados.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Estratégia de tabela única
@DiscriminatorColumn(name = "tipo_conta", discriminatorType = DiscriminatorType.STRING)
// Coluna que diferencia os tipos
public abstract class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private Long numero;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(nullable = false)
    private Boolean ativa;

    @ManyToOne(fetch = FetchType.LAZY) // evita que dados desnecessários sejam carregados do banco.
    @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name = "fk_conta_cliente"))
    @JsonIgnore // Evita serialização infinita
    private Cliente cliente;

    @Version // Anotação do JPA que habilita o controle de concorrência (Lock Otimista).
    private Long version; // O JPA irá gerenciar este campo automaticamente.

    public Conta(Long numero, BigDecimal saldo) {
        this.numero = numero;
        this.saldo = saldo;
    }

    // Métodos de negócio que podem ser sobrescritos
    public abstract void sacar(BigDecimal valor);

    public void depositar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.TEN) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que R$10,00.");
        }
        this.setSaldo(this.getSaldo().add(valor));
    }
}
