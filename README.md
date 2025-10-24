# API de Conta Bancária com Spring Boot

![Java](https://img.shields.io/badge/Java-21+-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg) ![Maven](https://img.shields.io/badge/Maven-blue.svg) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity) ![JWT](https://img.shields.io/badge/JWT-black?style=flat&logo=jsonwebtokens) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=swagger)

API RESTful para simulação de um sistema bancário, desenvolvida com Java e Spring Boot, seguindo princípios de Domain-Driven Design (DDD) e incluindo autenticação/autorização robusta com JWT.

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
* **📄 Documentação Interativa:** Interface **Swagger UI** para visualizar e testar os endpoints da API.
* **Robustez:** Validação de dados de entrada, tratamento de concorrência (Lock Otimista) e precisão monetária com `BigDecimal`.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21+**
* **Spring Boot 3**
* **Spring Data JPA / Hibernate**
* **Spring Security**
* **JSON Web Token (JWT) - JJWT Library**
* **SpringDoc OpenAPI (Swagger UI)**
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

4.  A API estará disponível em `http://localhost:8080`.
    * O console do banco H2 pode ser acessado em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`).
    * A documentação interativa do Swagger UI estará disponível em **`http://localhost:8080/swagger-ui.html`**.

---

## 🔑 Autenticação

* Use o endpoint `POST /auth/login` com o corpo `{"cpf": SEU_CPF, "senha": "SUA_SENHA"}` para obter um token JWT.
* Para acessar endpoints protegidos (identificados com um 🔒 no Swagger UI), clique no botão "Authorize" no canto superior direito do Swagger UI, cole seu token JWT (incluindo o prefixo `Bearer `) e clique em "Authorize" novamente.

---

## 📖 Documentação da API (Swagger)

A API possui uma documentação interativa gerada automaticamente com **Swagger UI (OpenAPI)**. Após executar a aplicação, acesse:

### **[➡️ http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Nesta página, você poderá:
* Visualizar todos os endpoints disponíveis, agrupados por funcionalidade (Tags).
* Ver os detalhes de cada endpoint: método HTTP, URL, parâmetros esperados, corpo da requisição e possíveis respostas (incluindo códigos de erro).
* Testar os endpoints diretamente pela interface, inclusive os protegidos (após realizar a autenticação via JWT, conforme explicado na seção "Autenticação").

---

## 📚 Documentação Técnica Completa

Para uma análise aprofundada da arquitetura, detalhamento de todas as classes, funções, conceitos (incluindo segurança) e um guia completo dos endpoints com permissões, acesse a nossa documentação técnica.

### **[➡️ Acesse a Documentação Completa aqui](./docs/DOCUMENTACAO_TECNICA.md)**
