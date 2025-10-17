package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {
    private final ClienteRepository clienteRepository;

    public AuthorizationService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long cpf = Long.parseLong(username);
            return clienteRepository.findByCpf(cpf)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o CPF: " + username));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Formato de CPF inválido: " + username);
        }
    }
}
