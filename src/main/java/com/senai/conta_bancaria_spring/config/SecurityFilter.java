package com.senai.conta_bancaria_spring.config;

import com.senai.conta_bancaria_spring.application.service.TokenService;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final ClienteRepository clienteRepository;

    public SecurityFilter(TokenService tokenService, ClienteRepository clienteRepository) {
        this.tokenService = tokenService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extrairToken(request);

        if (token != null) {
            try {
                String subject = tokenService.getSubject(token); // CPF do usuário
                Long cpf = Long.parseLong(subject);
                UserDetails user = clienteRepository.findByCpf(cpf).orElse(null);

                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token inválido (expirado, assinatura incorreta, etc.)
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
