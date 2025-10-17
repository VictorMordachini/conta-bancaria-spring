package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration-ms}")
    private long expirationMs;

    public String gerarToken(Cliente cliente) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setIssuer("API Conta Bancaria")
                .setSubject(cliente.getCpf().toString())
                .claim("role", cliente.getRole().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody()
                .getSubject();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}