# API de Conta Bancária com Spring Boot e Integração IoT

![Java](https://img.shields.io/badge/Java-21+-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg) ![Maven](https://img.shields.io/badge/Maven-blue.svg) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity) ![JWT](https://img.shields.io/badge/JWT-black?style=flat&logo=jsonwebtokens) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=swagger) ![MQTT](https://img.shields.io/badge/MQTT-Eclipse_Paho-purple.svg)

API RESTful robusta para simulação de um sistema bancário. Além das operações financeiras tradicionais, este projeto evoluiu para incluir recursos avançados como autenticação de dois fatores (2FA) via dispositivos IoT (simulados por MQTT), gerenciamento dinâmico de taxas e agendamento de transações.

---

## 🚀 Visão Geral das Funcionalidades

### 🏦 Funcionalidades Bancárias Principais
* **Gerenciamento de Clientes:** CRUD completo com validações.
* **Contas Bancárias:** Conta Corrente e Poupança com regras de negócio específicas.
* **Operações Financeiras:** Depósito, Saque, Transferência (PIX, TED, DOC) e Pagamentos.
* **Histórico e Extrato:** Registro detalhado de todas as transações.

### 🔐 Segurança Avançada & IoT
* **Autenticação JWT:** Login seguro com tokens de acesso.
* **Controle de Acesso (RBAC):** Perfis de `CLIENTE` e `GERENTE` com permissões distintas.
* **2FA via IoT:** Implementação de autenticação de dois fatores onde um dispositivo IoT físico (simulado) aprova operações críticas via mensagens **MQTT**.

### 💰 Gestão Financeira Aprimorada
* **Taxas Dinâmicas:** Sistema para cadastrar e gerenciar taxas aplicáveis a diferentes tipos de transação (ex: taxa para TED, isenção para PIX).
* **Transações Agendadas/Pendentes:** Suporte para agendar pagamentos e transferências, com processamento automático via *Scheduler*.

---

## 🛠️ Tecnologias Utilizadas

* **Core:** Java 21, Spring Boot 3, Spring Data JPA.
* **Segurança:** Spring Security, JWT (JJWT), BCrypt.
* **Integração:** **MQTT** (Eclipse Paho Client) para comunicação com dispositivos IoT.
* **Documentação:** SpringDoc OpenAPI (Swagger UI).
* **Banco de Dados:** H2 (em memória) para desenvolvimento rápido.
* **Agendamento:** Spring Scheduler para tarefas em segundo plano.

---

## 🏃‍♀️ Como Rodar a Aplicação

1.  **Pré-requisitos:**
    * Java JDK 21+.
    * Maven.
    * **Broker MQTT:** Para testar as funcionalidades de IoT, você precisará de um broker MQTT rodando localmente (ex: Mosquitto) na porta padrão `1883`, ou configurar um externo no `application.properties`.

2.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/victormordachini/api-conta-bancaria-spring.git](https://github.com/victormordachini/api-conta-bancaria-spring.git)
    cd api-conta-bancaria-spring
    ```

3.  **Execute a aplicação:**
    ```bash
    mvn spring-boot:run
    ```

4.  **Acesse:**
    * API: `http://localhost:8080`
    * Swagger UI: **`http://localhost:8080/swagger-ui.html`**
    * Banco H2: `http://localhost:8080/h2-console`

---

## 📱 Fluxo de Autenticação IoT (Simulado)

Para operações que exigem 2FA:
1.  O cliente solicita a operação na API.
2.  A API publica uma mensagem em um tópico MQTT específico para o dispositivo do cliente.
3.  O dispositivo (que pode ser simulado com um cliente MQTT como MQTTX) recebe a solicitação.
4.  O usuário "aprova" no dispositivo, que publica a confirmação de volta em outro tópico.
5.  A API recebe a confirmação e efetiva a transação.

---

## 📚 Documentação Técnica

Para detalhes sobre os endpoints, contratos de dados e configurações avançadas, consulte a nossa **[Documentação Técnica](./docs/DOCUMENTACAO_TECNICA.md)** ou acesse o **Swagger UI** com a aplicação em execução.
