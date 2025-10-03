package com.senai.conta_bancaria_spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "banco.conta-corrente")
@Getter
@Setter
public class BancoConfigProperties {
    private Long limitePadrao;
    private BigDecimal taxaPadrao;
}
