package com.senai.conta_bancaria_spring.domain.enums;

public enum UserRole {
    CLIENTE("CLIENTE"),
    GERENTE("GERENTE");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
