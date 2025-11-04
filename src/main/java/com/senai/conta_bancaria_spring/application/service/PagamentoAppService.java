package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.dto.PagamentoRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.PagamentoResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.enums.StatusPagamento;
import com.senai.conta_bancaria_spring.domain.exception.PagamentoInvalidoException;
import com.senai.conta_bancaria_spring.domain.exception.RecursoNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.exception.SaldoInsuficienteException;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import com.senai.conta_bancaria_spring.domain.repository.PagamentoRepository;
import com.senai.conta_bancaria_spring.domain.repository.TaxaRepository;
import com.senai.conta_bancaria_spring.domain.service.PagamentoDomainService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class PagamentoAppService {
    private final PagamentoDomainService pagamentoDomainService;
    private final ContaRepository contaRepository;
    private final TaxaRepository taxaRepository;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoAppService(PagamentoDomainService pagamentoDomainService,
                               ContaRepository contaRepository,
                               TaxaRepository taxaRepository,
                               PagamentoRepository pagamentoRepository) {
        this.pagamentoDomainService = pagamentoDomainService;
        this.contaRepository = contaRepository;
        this.taxaRepository = taxaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    public PagamentoResponseDTO realizarPagamento(Long numeroConta, PagamentoRequestDTO dto) {

        // 1. Buscar Entidade Conta
        Conta conta = contaRepository.findByNumero(numeroConta)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada: " + numeroConta));

        // 2. Validar Proprietário (Lógica de segurança)
        validarProprietarioDaConta(conta);

        // 3. Buscar Entidades Taxa
        Set<Taxa> taxas = new HashSet<>();
        if (dto.idsTaxas() != null && !dto.idsTaxas().isEmpty()) {
            taxas.addAll(taxaRepository.findAllById(dto.idsTaxas()));
            // Validação simples para garantir que todas as taxas pedidas foram encontradas
            if (taxas.size() != dto.idsTaxas().size()) {
                throw new RecursoNaoEncontradoException("Uma ou mais taxas não foram encontradas.");
            }
        }
        // Prepara o builder do pagamento para salvar em caso de SUCESSO ou FALHA
        Pagamento.PagamentoBuilder pagamentoBuilder = Pagamento.builder()
                .conta(conta)
                .boleto(dto.codigoBoleto())
                .valorPago(dto.valor())
                .taxas(taxas);

        BigDecimal custoTotal = BigDecimal.ZERO;

        try {
            // 4. Chamar Serviço de Domínio (onde a regra de negócio acontece)
            custoTotal = pagamentoDomainService.processarDebitoPagamento(
                    conta, dto.valor(), dto.codigoBoleto(), taxas
            );

            // 5. Salvar a conta (com saldo atualizado)
            contaRepository.save(conta);

            // 6. Criar e Salvar Pagamento com SUCESSO
            Pagamento pagamentoSucesso = pagamentoBuilder
                    .status(StatusPagamento.SUCESSO)
                    .build();
            pagamentoRepository.save(pagamentoSucesso);

            // 7. Retornar DTO de Sucesso
            return PagamentoResponseDTO.fromEntity(pagamentoSucesso, custoTotal);

        } catch (SaldoInsuficienteException | PagamentoInvalidoException e) {

            // 8. Determinar o Status da Falha
            StatusPagamento statusFalha;
            if (e instanceof SaldoInsuficienteException) {
                statusFalha = StatusPagamento.FALHA_SALDO_INSUFICIENTE;
            } else if (e.getMessage().contains("vencido")) {
                statusFalha = StatusPagamento.FALHA_BOLETO_VENCIDO;
            } else {
                statusFalha = StatusPagamento.FALHA_OPERACIONAL;
            }

            // 9. Criar e Salvar Pagamento com FALHA
            Pagamento pagamentoFalha = pagamentoBuilder
                    .status(statusFalha)
                    .build();
            pagamentoRepository.save(pagamentoFalha);

            // 10. Relançar a exceção para o GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * Valida se o cliente autenticado é o proprietário da conta.
     * (Lógica reutilizada do ContaServiceDomain)
     */
    private void validarProprietarioDaConta(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        Cliente clienteAutenticado = (Cliente) authentication.getPrincipal();
        String idClienteAutenticado = clienteAutenticado.getId();
        String idDonoDaConta = conta.getCliente().getId();

        if (!idClienteAutenticado.equals(idDonoDaConta)) {
            throw new AccessDeniedException("Acesso negado: Você não é o proprietário desta conta.");
        }
    }
}
