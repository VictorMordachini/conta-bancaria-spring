package com.senai.conta_bancaria_spring.infrastructure.scheduler;

import com.senai.conta_bancaria_spring.domain.repository.TransacaoPendenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@Slf4j
public class LimpezaPendenciasScheduler {

    private final TransacaoPendenteRepository transacaoPendenteRepository;

    public LimpezaPendenciasScheduler(TransacaoPendenteRepository transacaoPendenteRepository) {
        this.transacaoPendenteRepository = transacaoPendenteRepository;
    }

    // Executa a cada 10 minutos (600000 ms)
    @Scheduled(fixedRateString = "${app.scheduler.limpeza-pendencias.fixed-rate}")
    @Transactional
    public void limparTransacoesExpiradas() {
        // Define o limite como 10 minutos atrás. Tudo que for mais velho que isso e ainda estiver pendente, será apagado.
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);

        long countAntes = transacaoPendenteRepository.count();
        transacaoPendenteRepository.deleteByDataCriacaoBefore(limite);
        long countDepois = transacaoPendenteRepository.count();

        long deletados = countAntes - countDepois;

        if (deletados > 0) {
            log.info(">>> SCHEDULER: Limpeza de transações pendentes executada. {} registros expirados removidos.", deletados);
        } else {
            log.debug(">>> SCHEDULER: Limpeza de transações pendentes executada. Nenhum registro expirado.");
        }
    }
}
