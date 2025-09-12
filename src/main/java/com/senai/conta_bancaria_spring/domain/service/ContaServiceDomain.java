package com.senai.conta_bancaria_spring.domain.service;

import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

    @Transactional
    public void depositar(Long numeroConta, Double valor) {
        Conta conta = buscarPorNumero(numeroConta);
        conta.depositar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void sacar(Long numeroConta, Double valor) {
        Conta conta = buscarPorNumero(numeroConta);
        //O Java chama o método sacar() da classe concreta (ContaCorrente ou ContaPoupanca).
        conta.sacar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void transferir(Long numeroContaOrigem, Long numeroContaDestino, Double valor) {
        if (numeroContaOrigem.equals(numeroContaDestino)) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser as mesmas.");
        }

        Conta contaOrigem = buscarPorNumero(numeroContaOrigem);
        Conta contaDestino = buscarPorNumero(numeroContaDestino);

        // A regra de saque com taxa para Conta Corrente
        if (contaOrigem instanceof ContaCorrente) {
            double taxa = ((ContaCorrente) contaOrigem).getTaxa();
            double valorTotal = valor + (valor * taxa);
            if (valorTotal > contaOrigem.getSaldo() + ((ContaCorrente) contaOrigem).getLimite()){
                throw new IllegalStateException("Saldo insuficiente na conta de origem para a transferência com taxa.");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo() - valorTotal);
        } else {
            if (valor > contaOrigem.getSaldo()){
                throw new IllegalStateException("Saldo insuficiente na conta de origem para a transferência.");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);
        }

        contaDestino.setSaldo(contaDestino.getSaldo() + valor);

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);
    }


}
