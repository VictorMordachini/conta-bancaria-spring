package com.senai.conta_bancaria_spring.domain.service;

import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.exception.PagamentoInvalidoException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@Transactional
// Embora este serviço de domínio não acesse o DB, ele será chamado
// por um serviço de aplicação (Dia 4) que é transacional.
public class PagamentoDomainService {
    public BigDecimal processarDebitoPagamento(Conta conta, BigDecimal valorBoleto, String codigoBoleto, Set<Taxa> taxas) {

        // 1. Validar o Boleto
        validarBoleto(codigoBoleto, valorBoleto);

        // 2. Calcular o Custo Total
        BigDecimal custoTotal = calcularCustoTotal(valorBoleto, taxas);

        // 3. e 4. Validar Saldo e Debitar (POLIMORFISMO)
        // O serviço agora DELEGA a responsabilidade de validar o saldo
        // e aplicar o débito para a própria entidade Conta.
        // O Java chamará a implementação correta (ContaCorrente ou ContaPoupanca).
        conta.debitarPagamento(custoTotal);

        return custoTotal;
    }

    /**
     * Calcula o valor final de um pagamento somando as taxas (percentuais e fixas)
     * ao valor base do boleto.
     */
    private BigDecimal calcularCustoTotal(BigDecimal valorBase, Set<Taxa> taxas) {
        BigDecimal totalTaxas = BigDecimal.ZERO;

        if (taxas != null) {
            for (Taxa taxa : taxas) {
                // Calcula a taxa percentual sobre o valor base
                BigDecimal valorTaxaPercentual = valorBase.multiply(taxa.getPercentual());
                // Adiciona a taxa fixa
                BigDecimal valorTaxaFixa = taxa.getValorFixo();

                totalTaxas = totalTaxas.add(valorTaxaPercentual).add(valorTaxaFixa);
            }
        }

        // Custo total = Valor base + Soma de todas as taxas
        return valorBase.add(totalTaxas);
    }

    /**
     * Simula a validação de um boleto.
     */
    private void validarBoleto(String codigoBoleto, BigDecimal valorBoleto) {
        if (valorBoleto == null || valorBoleto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagamentoInvalidoException("O valor do pagamento deve ser positivo.");
        }

        if (codigoBoleto == null || codigoBoleto.isBlank()) {
            throw new PagamentoInvalidoException("O código do boleto é obrigatório.");
        }

        // Em um sistema real, aqui haveria uma chamada a um serviço externo
        // ou uma biblioteca de validação de linha digitável.

        // Simulação: Se o boleto começar com "VENCIDO", rejeita.
        if (codigoBoleto.startsWith("VENCIDO")) {
            throw new PagamentoInvalidoException("O boleto informado está vencido e não pode ser pago.");
        }
    }

}
