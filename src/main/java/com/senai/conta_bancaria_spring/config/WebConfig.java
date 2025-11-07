package com.senai.conta_bancaria_spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica o CORS a todos os endpoints
                .allowedOrigins("http://localhost:3000", "http://localhost:4200", "https_SEU_DOMINIO_PROD.com") // URLs permitidas
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Headers permitidos (ex: Authorization)
                .allowCredentials(true); // Permitir credenciais (cookies, tokens)
    }
}
