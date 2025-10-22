# API de Conta Bancária com Spring Boot

![Java](https://img.shields.io/badge/Java-21+-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg) ![Maven](https://img.shields.io/badge/Maven-blue.svg) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity) ![JWT](https://img.shields.io/badge/JWT-black?style=flat&logo=jsonwebtokens)

API RESTful para simulação de um sistema bancário, desenvolvida com Java e Spring Boot, seguindo princípios de Domain-Driven Design (DDD) e incluindo autenticação/autorização robusta.

---

## 🚀 Visão Geral das Funcionalidades

* **Gerenciamento de Clientes:** CRUD completo (Criar, Ler, Atualizar, Desativar).
* **Contas Bancárias:** Suporte para Conta Corrente e Conta Poupança com regras de negócio específicas (taxas, limites, rendimento).
* **Operações Financeiras:** Depósito, Saque e Transferência entre contas.
* **Histórico:** Geração de extrato bancário completo por conta.
* **🔐 Segurança:**
    * Autenticação via **JWT (JSON Web Token)**.
    * Autorização baseada em **papéis (Roles)**: `CLIENTE` e `GERENTE`.
    * Controle de acesso para garantir que clientes só acessem suas próprias contas.
    * Armazenamento seguro de senhas com **BCrypt**.
* **Robustez:** Validação de dados de entrada, tratamento de concorrência (Lock Otimista) e precisão monetária com `BigDecimal`.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21+**
* **Spring Boot 3**
* **Spring Data JPA / Hibernate**
* **Spring Security**
* **JSON Web Token (JWT) - JJWT Library**
* **Banco de Dados em Memória H2**
* **Lombok**
* **Maven**

---

## 🏃‍♀️ Como Rodar a Aplicação

1.  **Pré-requisitos:**
    * Java JDK 21 ou superior.
    * Apache Maven.

2.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/victormordachini/api-conta-bancaria-spring.git](https://github.com/victormordachini/api-conta-bancaria-spring.git)
    cd api-conta-bancaria-spring
    ```

3.  **Execute a aplicação com o Maven:**
    ```bash
    mvn spring-boot:run
    ```

4.  A API estará disponível em `http://localhost:8080`. O console do banco H2 pode ser acessado em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`).

---

## 🔑 Autenticação

* Use o endpoint `POST /auth/login` com o corpo `{"cpf": SEU_CPF, "senha": "SUA_SENHA"}` para obter um token JWT.
* Para acessar endpoints protegidos, inclua o token no cabeçalho `Authorization`:
    `Authorization: Bearer SEU_TOKEN_JWT`.

---

## 📚 Documentação Técnica Completa

Para uma análise aprofundada da arquitetura, detalhamento de todas as classes, funções, conceitos (incluindo segurança) e um guia completo dos endpoints com permissões, acesse a nossa documentação técnica.

### **[➡️ Acesse a Documentação Completa aqui](./docs/DOCUMENTACAO_TECNICA.md)**
