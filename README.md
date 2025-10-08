# API de Conta Bancária com Spring Boot

![Java](https://img.shields.io/badge/Java-21+-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg) ![Maven](https://img.shields.io/badge/Maven-blue.svg)

API RESTful para simulação de um sistema bancário, desenvolvida com Java e Spring Boot, seguindo princípios de Domain-Driven Design (DDD).

---

## 🚀 Visão Geral das Funcionalidades

* **Gerenciamento de Clientes:** CRUD completo (Criar, Ler, Atualizar, Desativar).
* **Contas Bancárias:** Suporte para Conta Corrente e Conta Poupança com regras de negócio específicas.
* **Operações Financeiras:** Depósito, Saque e Transferência entre contas.
* **Histórico:** Geração de extrato bancário completo por conta.
* **Robustez:** Validação de dados, tratamento de concorrência e precisão monetária com `BigDecimal`.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21+**
* **Spring Boot 3**
* **Spring Data JPA / Hibernate**
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
    git clone [https://github.com/VictorMordachini/api-conta-bancaria-spring.git](https://github.com/VictorMordachini/api-conta-bancaria-spring.git)
    cd api-conta-bancaria-spring
    ```

3.  **Execute a aplicação com o Maven:**
    ```bash
    mvn spring-boot:run
    ```

4.  A API estará disponível em `http://localhost:8080`.

---

## 📚 Documentação Técnica Completa

Para uma análise aprofundada da arquitetura, detalhamento de todas as classes, funções, conceitos e um guia completo dos endpoints, acesse a nossa documentação técnica.

### **[➡️ Acesse a Documentação Completa aqui](./docs/DOCUMENTACAO_TECNICA.md)**
