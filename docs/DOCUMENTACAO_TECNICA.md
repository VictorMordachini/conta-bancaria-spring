Documentação Mestra da API de Conta Bancária
Versão: 1.2.0 (Segura)
Data: 22 de outubro de 2025

Índice
Visão Geral e Arquitetura

Dicionário de Tecnologias, Conceitos e Padrões

Detalhamento das Classes e Funções

Guia de Endpoints da API

Conclusão e Próximos Passos

1. Visão Geral e Arquitetura
   A API foi projetada com uma arquitetura em camadas inspirada no Domain-Driven Design (DDD). Esta abordagem organiza o
   código de acordo com as responsabilidades de negócio, resultando em um sistema mais claro, flexível e fácil de
   manter.

domain (Domínio): O coração da aplicação. Contém toda a lógica de negócio e as regras que definem o que é uma conta
bancária, um cliente, etc. É totalmente independente das outras camadas.

application (Aplicação): O orquestrador. Serve como uma camada intermediária que coordena os casos de uso (ex: "criar um
novo cliente"), convertendo dados de entrada (DTOs) em ações no domínio.

interface_ui (Interface / Exposição): A fachada. É a camada que se comunica com o mundo exterior (neste caso, via
HTTP/JSON), expondo a API através de endpoints REST e tratando os erros de forma centralizada.

config: Contém classes para configurações externalizadas da aplicação, permitindo que regras de negócio (como valores
padrão) sejam alteradas sem modificar o código-fonte.

2. Dicionário de Tecnologias, Conceitos e Padrões
   Esta seção explica os "porquês" por trás das ferramentas e decisões técnicas.

A. Spring Boot e Spring Framework
@RestController, @RequestMapping, etc.: Mapeiam as URLs e os métodos HTTP para os métodos nos controllers, definindo os
endpoints da API.

Injeção de Dependência: O Spring gerencia a criação dos objetos (Beans) e os "injeta" onde são necessários (via
construtor). Por que usamos? Para criar um código desacoplado e fácil de testar.

@Transactional: Garante que um método seja executado dentro de uma transação de banco de dados. Por que usamos? É
crítico para operações financeiras para garantir a consistência dos dados (Atomicidade).

@ConfigurationProperties e @Value: Mecanismos para injetar valores do arquivo application.properties diretamente em
classes de configuração ou campos de serviços. Por que usamos? Para externalizar configurações, permitindo que regras de
negócio (como taxas e limites padrão) sejam alteradas sem recompilar o código.

GlobalExceptionHandler: Centraliza o tratamento de erros. Por que usamos? Para manter os controllers limpos e garantir
que todas as respostas de erro da API sejam consistentes e informativas.

@Valid e Jakarta Bean Validation: Valida os dados de entrada (DTOs) na "porta de entrada" da API. Por que usamos? Para
proteger a aplicação de dados inválidos e fornecer feedback claro ao cliente (400 Bad Request).

B. JPA / Hibernate (Persistência)
@Entity: Transforma uma classe Java em uma tabela no banco de dados.

@Inheritance(strategy = SINGLE_TABLE): Define que Conta e suas subclasses serão salvas na mesma tabela. Por que usamos?
É uma estratégia simples e performática, mas exige que colunas específicas de subclasses (limite, rendimento) permitam
valores nulos.

@Version (Lock Otimista): Adiciona um campo de versionamento à entidade. Por que usamos? Para prevenir condições de
corrida, garantindo que operações simultâneas na mesma conta não corrompam os dados.

C. Java e Boas Práticas (Clean Code)
BigDecimal: A única escolha correta para manipular dinheiro em Java, garantindo precisão nos cálculos.

enum (ex: TipoTransacao): Garante segurança de tipo e legibilidade para valores constantes.

@SuperBuilder (Lombok): Gera o código para o padrão "Builder", permitindo construir objetos de forma fluida e legível (
Objeto.builder().campo(valor).build()).

Polimorfismo: Em vez de usar if (objeto instanceof Classe), delegamos a responsabilidade da ação para a própria classe.
Por que usamos? Para criar um código mais limpo, coeso e extensível. Visto na refatoração do método transferir.

DRY (Don't Repeat Yourself): Princípio que visa eliminar a duplicação de código. Por que usamos? Para facilitar a
manutenção e evitar bugs. Visto na extração de lógicas repetitivas para métodos privados (ex: criarInstanciaDeConta).

D. Padrões de Projeto e Arquitetura
DTO (Data Transfer Object): Classes simples que carregam dados entre as camadas. Por que usamos? Para desacoplar a API
do nosso modelo de domínio.

Soft Delete: Marcar um registro como inativo (ativo = false) em vez de apagá-lo. Por que usamos? Para preservar o
histórico e a integridade dos dados.

E. Spring Security e JWT
@EnableWebSecurity, SecurityFilterChain: Configuração central que ativa a segurança web. É onde definimos quais endpoints são públicos (ex: `/auth/login`) e quais são protegidos por papéis (`ROLE_GERENTE`, `ROLE_CLIENTE`).

SessionCreationPolicy.STATELESS: Instrui o Spring Security a não criar sessões HTTP. Cada requisição deve se autenticar por conta própria (via token), tornando a API verdadeiramente RESTful.

PasswordEncoder (BCrypt): Define o algoritmo de criptografia assimétrica usado para armazenar senhas de forma segura. O BCrypt é o padrão da indústria por ser lento e resistente a ataques de força bruta.

UserDetailsService: Interface do Spring Security que usamos para conectar nossa lógica de busca de usuário (via `ClienteRepository`) ao framework de autenticação.

AccessDeniedHandler: Componente de tratamento de exceção focado em segurança. Nós o implementamos (`CustomAccessDeniedHandler`) para garantir que todos os erros 403 Forbidden (acesso negado) retornem uma resposta JSON padronizada, em vez de uma página de erro HTML ou um corpo vazio.

JWT (JSON Web Token): Padrão (RFC 7519) para criar tokens que afirmam um conjunto de informações (claims), como quem é o usuário (subject) e quais são seus papéis (roles). O token é assinado digitalmente pelo servidor, garantindo que não possa ser adulterado.

3. Detalhamento das Classes e Funções
   Um tour guiado pelo código da aplicação.

A. Camada de Domínio (domain)
entity.Conta: Classe abstrata base. Agora contém os métodos protected de validação (validarValorDebitoPositivo,
validarSaldoSuficiente, etc.) para centralizar regras de negócio comuns e o novo contrato abstract
debitarParaTransferencia.

entity.ContaCorrente / ContaPoupanca: Implementações concretas que agora contêm sua própria lógica de débito para
transferências e saques, utilizando os métodos de validação herdados. ContaCorrente também possui o método
atualizarParametros para alterar seu estado.

service.ContaServiceDomain: Serviço de Domínio. O método transferir foi refatorado e está muito mais limpo, usando
polimorfismo. Foi adicionado o método atualizarParametrosContaCorrente para orquestrar a atualização de uma conta.

exception.RecursoNaoEncontradoException: Exceção customizada e semântica para erros de "não encontrado", permitindo que
a API retorne o status 404 Not Found.

entity.Cliente: A entidade Cliente foi significativamente atualizada. Agora implementa a interface `UserDetails` do Spring Security, conectando nosso modelo de domínio diretamente ao framework de autenticação. Inclui os novos campos `senha` (armazenada com BCrypt) e `role` (um Enum `UserRole` para definir se é `CLIENTE` ou `GERENTE`).

service.ContaServiceDomain: O serviço de domínio agora implementa uma verificação de posse de recurso. O novo método `validarProprietarioDaConta` é chamado no início de todas as operações de conta (sacar, depositar, extrato) para garantir que um usuário autenticado só possa modificar ou ver as contas que lhe pertencem.

B. Camada de Aplicação (application)
dto.ContaCorrenteUpdateRequestDTO: Novo DTO para a atualização parcial de uma conta corrente.

service.ClienteService: Serviço de Aplicação. Agora injeta as configurações do banco (BancoConfigProperties e @Value)
para usar os valores padrão ao criar contas. A lógica de criação foi centralizada no método privado
criarInstanciaDeConta, e a busca de clientes foi centralizada em buscarClientePorIdOuFalhar, ambos aplicando o princípio
DRY.

service.AuthorizationService: Nova classe que implementa `UserDetailsService`. Sua única responsabilidade é carregar um `Cliente` (pelo CPF, que tratamos como username) e entregá-lo ao Spring Security.

service.TokenService: Nova classe responsável por todo o ciclo de vida do JWT. Gera um token após o login bem-sucedido e valida o token em requisições subsequentes.

C. Camada de Interface (interface_ui)
controller.ContaController: Foi adicionado o novo endpoint PATCH /contas/corrente/{numeroConta} para permitir a
atualização de limite e taxa.

exception.GlobalExceptionHandler: Agora possui um ExceptionHandler específico para RecursoNaoEncontradoException,
garantindo o retorno do status 404 Not Found. O tratamento da `AccessDeniedException` foi removido desta classe e centralizado no `CustomAccessDeniedHandler` para capturar *todos* os erros de acesso, incluindo os gerados pelos filtros do Spring Security.

controller.AuthenticationController: Novo controller que expõe o endpoint público `POST /auth/login`. Ele recebe as credenciais, autentica o usuário e retorna o JWT gerado pelo `TokenService`.

D. Camada de Configuração (config)
config.BancoConfigProperties: Nova classe que mapeia e agrupa as propriedades de configuração do application.properties
relacionadas aos padrões da conta corrente.

SecurityConfig: A classe de configuração principal da segurança. Define o `PasswordEncoder`, o `AuthenticationManager` e as regras de autorização (`authorizeHttpRequests`) para cada endpoint.

SecurityFilter: Um filtro que intercepta *todas* as requisições. Ele extrai o token JWT do cabeçalho `Authorization`, valida-o usando o `TokenService` e, se for válido, define o usuário no contexto de segurança para aquela requisição.

CustomAccessDeniedHandler: Implementação personalizada que garante que qualquer erro `403 Forbidden` na aplicação (seja por regras de filtro ou lógica de serviço) retorne uma resposta JSON padronizada.

4. Guia de Endpoints da API
   Todas as requisições, exceto as marcadas como "PÚBLICO", exigem um cabeçalho de autenticação:
   `Authorization: Bearer <SEU_TOKEN_JWT>`

   Funcionalidade              Método HTTP / URL                       Permissão     Corpo (Exemplo JSON)
   -------------------------    -----------------------------------     -----------   -------------------------------------------------
   Autenticar Usuário           POST /auth/login                        PÚBLICO       { "cpf": ..., "senha": "..." }
   Criar Novo Cliente           POST /clientes                          PÚBLICO       { "nome": "...", "cpf": ..., "senha": "...", ... }
   Listar Clientes Ativos       GET /clientes                           GERENTE       N/A
   Buscar Cliente por ID        GET /clientes/{id}                      GERENTE       N/A
   Atualizar Cliente            PUT /clientes/{id}                      GERENTE       { "nome": "Novo Nome" }
   Desativar Cliente            DELETE /clientes/{id}                   GERENTE       N/A
   Abrir Nova Conta             POST /clientes/{clienteId}/contas       GERENTE       { "tipoConta": "Poupanca", ... }
   Depositar                    POST /contas/{numeroConta}/depositar    CLIENTE       { "valor": "100.00" } (Apenas na própria conta)
   Sacar                        POST /contas/{numeroConta}/sacar        CLIENTE       { "valor": "50.00" } (Apenas na própria conta)
   Transferir                   POST /contas/{numContaOrigem}/transferir CLIENTE      { "numeroContaDestino": ..., ... } (Apenas da própria conta)
   Consultar Extrato            GET /contas/{numeroConta}/extrato       CLIENTE       N/A (Apenas da própria conta)
   Atualizar C. Corrente        PATCH /contas/corrente/{numeroConta}    CLIENTE       { "limite": 2000 } (Apenas na própria conta)

5. Conclusão e Próximos Passos
   A API em seu estado atual é uma aplicação robusta, funcional e segura, cobrindo os casos de uso essenciais de um sistema bancário. A implementação do Spring Security com JWT e controle de acesso baseado em papéis (RBAC) garante a correta segregação de funções (Gerente vs. Cliente) e a posse de dados (um cliente só pode ver suas próprias contas).

   Próximos passos naturais na evolução do projeto seriam:
   1.  **Testes Automatizados**: Implementação de uma suíte de testes unitários (JUnit/Mockito) para os serviços e testes de integração (com `@SpringBootTest`) para os controllers, cobrindo os cenários de segurança.
   2.  **Paginação**: Adicionar paginação ao endpoint `GET /clientes` para otimizar o desempenho.
   3.  **Dockerização**: Criar um `Dockerfile` para conteinerizar a aplicação, facilitando o deploy.