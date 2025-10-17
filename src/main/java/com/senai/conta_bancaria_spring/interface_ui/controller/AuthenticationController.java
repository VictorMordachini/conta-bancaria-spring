package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.dto.LoginRequestDTO;
import com.senai.conta_bancaria_spring.application.dto.TokenResponseDTO;
import com.senai.conta_bancaria_spring.application.service.TokenService;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.cpf().toString(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var cliente = (Cliente) auth.getPrincipal();
        var token = tokenService.gerarToken(cliente);

        return ResponseEntity.ok(new TokenResponseDTO(token));
    }
}
