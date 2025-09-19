package com.senai.conta_bancaria_spring.domain.service;

import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Transactional
@Service
public class ContaServiceDomain {
    private final ContaRepository contaRepository;

    public ContaServiceDomain(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public Conta buscarPorNumero(Long numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada."));
    }

    public void depositar(Long numeroConta, BigDecimal valor) {
        Conta conta = buscarPorNumero(numeroConta);
        conta.depositar(valor);
        contaRepository.save(conta);
    }

    public void sacar(Long numeroConta, BigDecimal valor) {
        Conta conta = buscarPorNumero(numeroConta);
        //O Java chama o método sacar() da classe concreta (ContaCorrente ou ContaPoupanca).
        conta.sacar(valor);
        contaRepository.save(conta);
    }

    public void transferir(Long numeroContaOrigem, Long numeroContaDestino, BigDecimal valor) {
        if (numeroContaOrigem.equals(numeroContaDestino)) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser as mesmas.");
        }

        Conta contaOrigem = buscarPorNumero(numeroContaOrigem);
        Conta contaDestino = buscarPorNumero(numeroContaDestino);

        // A regra de saque com taxa para Conta Corrente
        if (contaOrigem instanceof ContaCorrente cc) {
            BigDecimal taxa = cc.getTaxa();
            BigDecimal valorTotal = valor.add(valor.multiply(taxa));
            BigDecimal saldoDisponivel = cc.getSaldo().add(BigDecimal.valueOf(cc.getLimite()));

            if (valorTotal.compareTo(saldoDisponivel) > 0) {
                throw new IllegalStateException("Saldo insuficiente na conta de origem para a transferência com taxa.");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valorTotal));
        } else {
            if (valor.compareTo(contaOrigem.getSaldo()) > 0) {
                throw new IllegalStateException("Saldo insuficiente na conta de origem para a transferência.");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        }

        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);
    }


}
