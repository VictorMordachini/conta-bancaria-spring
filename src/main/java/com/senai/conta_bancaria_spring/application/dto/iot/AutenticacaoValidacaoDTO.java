package com.senai.conta_bancaria_spring.application.dto.iot;

public record AutenticacaoValidacaoDTO(
        String clienteId,
        String codigoValidado,
        Boolean biometriaOk
) {
}
