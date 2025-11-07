package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.service.SseNotificacaoService;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "5. Notificações (SSE)", description = "Endpoints para notificações em tempo real.")
@SecurityRequirement(name = "bearerAuth") // Exige autenticação (cadeado no Swagger)
public class NotificacaoController {
    private final SseNotificacaoService sseService;

    public NotificacaoController(SseNotificacaoService sseService) {
        this.sseService = sseService;
    }

    @Operation(summary = "Assina o feed de notificações (CLIENTE)",
            description = "Estabelece uma conexão Server-Sent Events (SSE) para receber " +
                    "notificações em tempo real sobre o status de operações assíncronas (saque, pagamento, etc).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conexão estabelecida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter assinarNotificacoes() {
        // Pega o cliente autenticado da mesma forma que o ContaServiceDomain faz
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Cliente clienteAutenticado = (Cliente) authentication.getPrincipal();
        String clienteId = clienteAutenticado.getId();

        return sseService.criarEmitter(clienteId);
    }
}
