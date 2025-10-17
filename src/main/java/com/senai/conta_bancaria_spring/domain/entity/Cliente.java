package com.senai.conta_bancaria_spring.domain.entity;

import com.senai.conta_bancaria_spring.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "clientes",
        uniqueConstraints = @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf"))
public class Cliente implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, nullable = false, length = 11)
    private Long cpf;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Conta> contas = new ArrayList<>();

    @Column(nullable = false)
    private Boolean ativo = true;

    public void adicionarConta(Conta conta) {
        this.contas.add(conta);
        conta.setCliente(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.GERENTE) {
            return List.of(new SimpleGrantedAuthority("ROLE_GERENTE"), new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.cpf.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }

}
