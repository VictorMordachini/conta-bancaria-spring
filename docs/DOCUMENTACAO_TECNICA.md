# Documentação Mestra da API de Conta Bancária
**Versão:** 2.0.0 (Com Autenticação IoT e Pagamentos)
**Data:** 06 de novembro de 2025

## Índice
1. [Visão Geral e Arquitetura](#1-visão-geral-e-arquitetura)
2. [Dicionário de Tecnologias, Conceitos e Padrões](#2-dicionário-de-tecnologias-conceitos-e-padrões)
3. [Detalhamento das Classes e Funções](#3-detalhamento-das-classes-e-funções)
4. [Fluxos de Autenticação e Segurança](#4-fluxos-de-autenticação-e-segurança)
5. [Guia de Endpoints da API](#5-guia-de-endpoints-da-api)
6. [Conclusão e Próximos Passos](#6-conclusão-e-próximos-passos)

---

## 1. Visão Geral e Arquitetura
A API foi projetada com uma arquitetura em camadas inspirada no Domain-Driven Design (DDD), organizando o código por responsabilidades de negócio para clareza, flexibilidade e manutenibilidade.

* **domain (Domínio):** O coração da aplicação, contendo a lógica de negócio e regras independentes (Entidades, Enums, Repositórios, Services de Domínio).
* **application (Aplicação):** Camada orquestradora que coordena casos de uso, convertendo DTOs em ações de domínio.
* **interface_ui (Interface / Exposição):** Fachada de comunicação externa (Controllers REST, Exception Handlers).
* **infrastructure (Infraestrutura):** Implementações técnicas que dão suporte ao domínio, como comunicação MQTT e agendadores.
* **config:** Configurações externalizadas e beans do Spring (Segurança, Swagger, Propriedades).

---

## 2. Dicionário de Tecnologias, Conceitos e Padrões

### A. Spring Ecosystem
* **Spring Boot 3 & Java 21:** Base moderna e robusta para a aplicação.
* **Spring Security & JWT:** Autenticação stateless via tokens JWT e autorização baseada em papéis (RBAC: `ROLE_GERENTE`, `ROLE_CLIENTE`).
* **Spring Data JPA:** Abstração para persistência de dados, utilizando Hibernate como provedor.
* **Spring Integration MQTT (via `spring-mqttx`):** Biblioteca para simplificar a comunicação assíncrona com brokers MQTT.

### B. Padrões de Projeto e Arquitetura
* **DDD (Domain-Driven Design):** Foco no núcleo do negócio, com entidades ricas e serviços de domínio para lógicas complexas.
* **Polimorfismo:** Utilizado nas operações de conta (`ContaCorrente` vs `ContaPoupanca`) para delegar regras específicas (ex: limite, rendimento).
* **DTO (Data Transfer Object):** Desacoplamento entre a camada de apresentação (API) e o modelo de domínio.
* **Saga / Processo de Negócio Longo (Assíncrono):** Implementado para operações financeiras que requerem validação externa (IoT), onde a requisição inicial retorna `202 Accepted` e a conclusão ocorre posteriormente via mensagem MQTT.

### C. Conceitos Chave
* **Autenticação IoT (2FA Assíncrono):** Operações críticas (saque, transferência, pagamento) exigem uma segunda validação biométrica em um dispositivo físico do cliente.
* **Transação Pendente:** Estado intermediário de uma operação financeira aguardando a confirmação IoT.
* **Soft Delete:** Preservação de histórico ao desativar clientes em vez de excluí-los fisicamente.
* **Lock Otimista (`@Version`):** Prevenção de concorrência desleal na atualização de saldos.

---

## 3. Detalhamento das Classes e Funções

### A. Camada de Domínio (domain)
* **Entidades Principais:** `Cliente`, `Conta` (abstrata), `ContaCorrente`, `ContaPoupanca`, `Transacao`.
* **Novas Entidades (Sprint 2):**
   * `Pagamento`: Registra pagamentos de boletos.
   * `Taxa`: Define custos adicionais aplicáveis a pagamentos.
   * `DispositivoIoT` & `CodigoAutenticacao`: Gerenciam a segurança física e códigos 2FA.
   * `TransacaoPendente`: Armazena temporariamente operações aguardando validação IoT.
* **Serviços de Domínio:**
   * `ContaServiceDomain`: Lógica central de contas (depósito, saque, transferência).
   * `PagamentoDomainService`: Lógica de cálculo de pagamentos (valor + taxas) e validações.

### B. Camada de Aplicação (application)
* **Serviços Orquestradores:**
   * `ClienteService`: Gerenciamento de clientes e abertura de contas.
   * `PagamentoAppService`: Coordena o fluxo de pagamento, persistindo sucesso ou falha.
   * `AutenticacaoIoTService`: Gera códigos 2FA e valida respostas dos dispositivos.
   * `TransacaoPendenteService`: Gerencia o ciclo de vida das operações em espera.

### C. Camada de Infraestrutura (infrastructure)
* `MqttPublisherService`: Envia solicitações de autenticação para os dispositivos via tópico MQTT.
* `MqttListenerService`: Escuta confirmações biométricas e dispara a execução das transações pendentes.
* `LimpezaPendenciasScheduler`: Tarefa agendada para remover transações que expiraram sem validação.

---

## 4. Fluxos de Autenticação e Segurança

### A. Autenticação de Usuário (JWT)
1.  Cliente envia CPF/Senha para `/auth/login`.
2.  API valida e retorna um **Token JWT**.
3.  Cliente usa o token no header `Authorization: Bearer ...` para requisições subsequentes.

### B. Autenticação de Operação (IoT / MQTT)
1.  Cliente solicita operação (ex: `POST /contas/123/sacar`).
2.  API gera um `CodigoAutenticacao`, salva uma `TransacaoPendente` e envia solicitação via MQTT para o dispositivo do cliente.
3.  API retorna imediatamente `HTTP 202 Accepted`.
4.  Cliente valida biometria no dispositivo físico.
5.  Dispositivo envia confirmação via MQTT para a API.
6.  `MqttListenerService` recebe a confirmação, valida o código e executa a operação financeira real (debitando a conta).

---

## 5. Guia de Endpoints da API

> **Nota:** Todos os endpoints protegidos exigem o header `Authorization: Bearer <TOKEN_JWT>`.

| Funcionalidade | Método | URL | Permissão | Status Típico |
| :--- | :--- | :--- | :--- | :--- |
| **Autenticação** | POST | `/auth/login` | PÚBLICO | `200 OK` |
| **Clientes** | POST | `/clientes` | PÚBLICO | `201 Created` |
| | GET | `/clientes` | GERENTE | `200 OK` |
| | GET | `/clientes/{id}` | GERENTE | `200 OK` |
| | PUT | `/clientes/{id}` | GERENTE | `200 OK` |
| | DELETE | `/clientes/{id}` | GERENTE | `204 No Content` |
| | POST | `/clientes/{id}/contas`| GERENTE | `201 Created` |
| **Taxas** | POST | `/taxas` | GERENTE | `201 Created` |
| | GET | `/taxas` | GERENTE | `200 OK` |
| | PUT | `/taxas/{id}` | GERENTE | `200 OK` |
| | DELETE | `/taxas/{id}` | GERENTE | `204 No Content` |
| **Contas (Operações)**| POST | `/contas/{num}/depositar`| CLIENTE (Dono)| `200 OK` |
| | POST | `/contas/{num}/sacar` | CLIENTE (Dono)| `202 Accepted`*|
| | POST | `/contas/{org}/transferir`| CLIENTE (Dono)| `202 Accepted`*|
| | POST | `/contas/{num}/pagar` | CLIENTE (Dono)| `202 Accepted`*|
| | GET | `/contas/{num}/extrato` | CLIENTE (Dono)| `200 OK` |
| | PATCH | `/contas/corrente/{num}`| CLIENTE (Dono)| `200 OK` |

\* *Operações assíncronas que requerem validação IoT subsequente.*

---

## 6. Conclusão e Próximos Passos
A versão 2.0.0 da API representa um avanço significativo em segurança e funcionalidade, simulando um ambiente bancário moderno com validação em duas etapas via hardware (IoT) e operações financeiras complexas (pagamentos com taxas variáveis).

**Próximos Passos Sugeridos:**
1.  **Notificações em Tempo Real:** Implementar WebSockets ou Server-Sent Events (SSE) para notificar o cliente front-end assim que a operação assíncrona (IoT) for concluída com sucesso ou falha.
2.  **Testes de Integração IoT:** Criar cenários de teste automatizados que simulem o comportamento do broker MQTT e dos dispositivos físicos.
3.  **Dashboard Gerencial:** Desenvolver uma interface web para que gerentes possam visualizar métricas de operações, transações pendentes e gerenciar taxas de forma visual.