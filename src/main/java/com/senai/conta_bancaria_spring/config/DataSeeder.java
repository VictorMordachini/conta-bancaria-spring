package com.senai.conta_bancaria_spring.config;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.enums.UserRole;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j; // Import do Lombok para logs
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
@Slf4j // 1. Anotação que cria automaticamente o objeto 'log'
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByCpf(99999999999L).isEmpty()) {
                Cliente gerente = new Cliente();
                gerente.setNome("Gerente Admin");
                gerente.setCpf(99999999999L);
                gerente.setSenha(passwordEncoder.encode("admin123"));
                gerente.setRole(UserRole.GERENTE);
                gerente.setAtivo(true);

                // Não é necessário instanciar contas.
                // A classe Cliente já inicializa 'contas' com new ArrayList<>(),
                // o que é suficiente para o login funcionar sem erros de NullPointerException.

                repository.save(gerente);

                // 2. Uso correto de logs em vez de System.out.println
                log.info(">>> [DEV-MODE] Gerente de teste criado com sucesso.");
                log.info(">>> CPF: 99999999999 | Senha: admin123");
            } else {
                log.info(">>> [DEV-MODE] Gerente de teste já existente. Pulei a criação.");
            }
        };
    }
}