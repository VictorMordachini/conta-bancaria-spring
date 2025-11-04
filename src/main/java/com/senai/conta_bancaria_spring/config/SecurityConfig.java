package com.senai.conta_bancaria_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler; // <-- 1. INJETAR O NOVO HANDLER

    public SecurityConfig(SecurityFilter securityFilter, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.securityFilter = securityFilter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                .exceptionHandling(exceptions ->
                        exceptions.accessDeniedHandler(customAccessDeniedHandler)
                )

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()

                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/clientes").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Endpoints de GERENTE
                        .requestMatchers(HttpMethod.GET, "/clientes").hasAuthority("ROLE_GERENTE")
                        .requestMatchers(HttpMethod.GET, "/clientes/{id}").hasAuthority("ROLE_GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/clientes/{id}").hasAuthority("ROLE_GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/clientes/{id}").hasAuthority("ROLE_GERENTE")
                        .requestMatchers(HttpMethod.POST, "/clientes/{clienteId}/contas").hasAuthority("ROLE_GERENTE")

                        .requestMatchers("/taxas/**").hasAuthority("ROLE_GERENTE")

                        // Endpoints de CLIENTE
                        .requestMatchers("/contas/**").hasAuthority("ROLE_CLIENTE")

                        // Qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}