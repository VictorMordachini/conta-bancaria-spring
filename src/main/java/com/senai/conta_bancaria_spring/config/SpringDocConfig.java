package com.senai.conta_bancaria_spring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // Informações baseadas no seu README.md e DOCUMENTACAO_TECNICA.md
        Info info = new Info()
                .title("API de Conta Bancária")
                .version("1.2.0")
                .description("API RESTful para simulação de um sistema bancário, " +
                        "desenvolvida com Java e Spring Boot, seguindo princípios de DDD " +
                        "e incluindo autenticação/autorização com JWT.");

        // Define o esquema de segurança (Bearer Token)
        final String securitySchemeName = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Adiciona o esquema de segurança aos componentes
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, securityScheme);

        // Adiciona o requisito de segurança global (o cadeado em todos os endpoints)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
