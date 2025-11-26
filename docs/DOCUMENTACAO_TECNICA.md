# Documentação Mestra da API de Conta Bancária
**Versão:** 2.1.0 (Com Autenticação IoT, Pagamentos e Notificações Real-Time)
**Data:** 26 de novembro de 2025

## Índice
1. [Visão Geral e Arquitetura](#1-visão-geral-e-arquitetura)
2. [Dicionário de Tecnologias, Conceitos e Padrões](#2-dicionário-de-tecnologias-conceitos-e-padrões)
3. [Detalhamento das Classes e Funções](#3-detalhamento-das-classes-e-funções)
4. [Fluxos de Autenticação e Segurança](#4-fluxos-de-autenticação-e-segurança)
5. [Guia de Endpoints da API](#5-guia-de-endpoints-da-api)
6. [Conclusão e Próximos Passos (Roadmap Técnico)](#6-conclusão-e-próximos-passos-roadmap-técnico)

---

## 1. Visão Geral e Arquitetura
A API foi projetada com uma arquitetura em camadas inspirada no Domain-Driven Design (DDD), organizando o código por responsabilidades de negócio para clareza, flexibilidade e manutenibilidade.

* **domain (Domínio):** O coração da aplicação, contendo a lógica de negócio e regras independentes (Entidades, Enums, Repositórios, Services de Domínio).
* **application (Aplicação):** Camada orquestradora que coordena casos de uso, convertendo DTOs em ações de domínio.
* **interface_ui (Interface / Exposição):** Fachada de comunicação externa (Controllers REST, Exception Handlers, Notificações).
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
* **Saga / Processo de Negócio Longo (Assíncrono):** Implementado para operações financeiras que requerem validação externa (IoT), onde a requisição inicial retorna `202 Accepted` e a conclusão ocorre posteriormente.

### C. Conceitos Chave
* **Autenticação IoT (2FA Assíncrono):** Operações críticas (saque, transferência, pagamento) exigem uma segunda validação biométrica em um dispositivo físico do cliente.
* **Transação Pendente:** Estado intermediário de uma operação financeira aguardando a confirmação IoT.
* **Feedback em Tempo Real (SSE):** Utilização de **Server-Sent Events** para notificar o cliente front-end instantaneamente sobre o sucesso ou falha da validação IoT, eliminando a necessidade de *polling*.
* **Lock Otimista (`@Version`):** Prevenção de concorrência desleal na atualização de saldos.

---

## 3. Detalhamento das Classes e Funções

### A. Camada de Domínio (domain)
* **Entidades Principais:** `Cliente`, `Conta` (abstrata), `ContaCorrente`, `ContaPoupanca`, `Transacao`.
* **Entidades de Suporte:**
  * `Pagamento` e `Taxa`: Gerenciamento de boletos e custos variáveis.
  * `DispositivoIoT` & `CodigoAutenticacao`: Segurança física e 2FA.
  * `TransacaoPendente`: Armazena operações aguardando validação IoT.
* **Serviços de Domínio:**
  * `ContaServiceDomain`: Lógica central de contas (depósito, saque, transferência).
  * `PagamentoDomainService`: Cálculo de valores e validações financeiras.

### B. Camada de Aplicação (application)
* **Serviços Orquestradores:**
  * `ClienteService`: Gerenciamento de clientes e contas.
  * `PagamentoAppService`: Coordena fluxos de pagamento.
  * `AutenticacaoIoTService`: Validação de códigos 2FA.
  * `SseNotificacaoService`: Gerencia emissores de eventos para notificações push ao front-end.

### C. Camada de Infraestrutura (infrastructure)
* `MqttPublisherService`: Envia solicitações de autenticação ao dispositivo.
* `MqttListenerService`: Escuta confirmações do dispositivo, executa a transação pendente e dispara a notificação SSE de sucesso/falha.
* `LimpezaPendenciasScheduler`: Tarefa agendada para remover transações expiradas.

---

## 4. Fluxos de Autenticação e Segurança

### A. Autenticação de Usuário (JWT)
1.  Cliente envia credenciais para `/auth/login`.
2.  API retorna Token JWT.
3.  Cliente usa o token no header `Authorization` para requisições.

### B. Fluxo Completo de Operação (IoT + SSE)
1.  **Início:** Cliente solicita operação (ex: Pagar Boleto) via REST.
2.  **Processamento:** API cria `TransacaoPendente`, envia solicitação MQTT ao dispositivo e retorna `202 Accepted` imediatamente.
3.  **Espera:** O Frontend conecta em `/notificacoes/sse` e aguarda.
4.  **Validação Física:** Usuário valida biometria no dispositivo IoT.
5.  **Confirmação:** Dispositivo publica resposta no tópico MQTT.
6.  **Conclusão:** API recebe a mensagem MQTT, efetiva o pagamento e envia um evento **SSE** ao frontend: `"Pagamento concluído com sucesso"`.

---

## 5. Guia de Endpoints da API

> **Nota:** Todos os endpoints protegidos exigem autenticação Bearer Token.

| Funcionalidade | Método | URL | Permissão | Status Típico |
| :--- | :--- | :--- | :--- | :--- |
| **Autenticação** | POST | `/auth/login` | PÚBLICO | `200 OK` |
| **Notificações** | GET | `/notificacoes/sse` | CLIENTE | `200 OK` (Stream) |
| **Clientes** | POST | `/clientes` | PÚBLICO | `201 Created` |
| | GET | `/clientes` | GERENTE | `200 OK` |
| **Taxas** | POST | `/taxas` | GERENTE | `201 Created` |
| | GET | `/taxas` | GERENTE | `200 OK` |
| **Contas (Operações)**| POST | `/contas/{num}/depositar`| CLIENTE | `200 OK` |
| | POST | `/contas/{num}/sacar` | CLIENTE | `202 Accepted`*|
| | POST | `/contas/{num}/pagar` | CLIENTE | `202 Accepted`*|
| | GET | `/contas/{num}/extrato` | CLIENTE | `200 OK` |

\* *Operações assíncronas: O resultado final é enviado via SSE.*

---

## 6. Conclusão e Próximos Passos (Roadmap Técnico)

A versão 2.1.0 consolida as funcionalidades de negócio. O foco agora se volta para a excelência técnica, extensibilidade e infraestrutura.

### Fase 1: Refatoração e Clean Code (Imediato)
1.  **Eliminação de Magic Strings:** Substituir literais como "Corrente" por Enums (`TipoConta`), garantindo Type Safety.
2.  **Padronização com Factory:** Implementar `ContaFactory` para centralizar a lógica complexa de criação de contas, removendo responsabilidades do `ClienteService`.
3.  **Desacoplamento de Segurança:** Extrair validações de `SecurityContext` dos serviços de domínio para um `SecurityValidator` na camada de aplicação.

### Fase 2: Arquitetura Escalável (Curto Prazo)
1.  **Strategy Pattern no MQTT:** Substituir o `switch/case` no `MqttListenerService` por uma implementação do padrão **Strategy** (`SaqueStrategy`, `PagamentoStrategy`), permitindo adicionar novas operações (como PIX) sem modificar o código existente (Open/Closed Principle).

### Fase 3: DevOps e Testabilidade (Médio Prazo)
1.  **Containerização:** Criar `Dockerfile` e `docker-compose.yml` para orquestrar a API, o Banco de Dados e o Broker MQTT em um único comando.
2.  **Testes de Integração:** Implementar **Testcontainers** para validar o fluxo IoT completo (API <-> Broker MQTT) em ambiente de CI/CD.