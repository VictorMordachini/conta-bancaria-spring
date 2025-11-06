package com.senai.conta_bancaria_spring.infrastructure.scheduler;

import com.senai.conta_bancaria_spring.domain.repository.TransacaoPendenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LimpezaPendenciasScheduler {

    private final TransacaoPendenteRepository transacaoPendenteRepository;

    public LimpezaPendenciasScheduler(TransacaoPendenteRepository transacaoPendenteRepository) {
        this.transacaoPendenteRepository = transacaoPendenteRepository;
    }

    // Executa a cada 10 minutos (600000 ms)
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void limparTransacoesExpiradas() {
        // Define o limite como 10 minutos atrás. Tudo que for mais velho que isso e ainda estiver pendente, será apagado.
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);

        transacaoPendenteRepository.deleteByDataCriacaoBefore(limite);

        System.out.println(">>> SCHEDULER: Limpeza de transações pendentes antigas executada.");
    }
}
