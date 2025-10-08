Documentação Mestra da API de Conta Bancária
Versão: 1.1.0 (Estável)
Data: 3 de outubro de 2025

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

B. Camada de Aplicação (application)
dto.ContaCorrenteUpdateRequestDTO: Novo DTO para a atualização parcial de uma conta corrente.

service.ClienteService: Serviço de Aplicação. Agora injeta as configurações do banco (BancoConfigProperties e @Value)
para usar os valores padrão ao criar contas. A lógica de criação foi centralizada no método privado
criarInstanciaDeConta, e a busca de clientes foi centralizada em buscarClientePorIdOuFalhar, ambos aplicando o princípio
DRY.

C. Camada de Interface (interface_ui)
controller.ContaController: Foi adicionado o novo endpoint PATCH /contas/corrente/{numeroConta} para permitir a
atualização de limite e taxa.

exception.GlobalExceptionHandler: Agora possui um ExceptionHandler específico para RecursoNaoEncontradoException,
garantindo o retorno do status 404 Not Found.

D. Camada de Configuração (config)
config.BancoConfigProperties: Nova classe que mapeia e agrupa as propriedades de configuração do application.properties
relacionadas aos padrões da conta corrente.

4. Guia de Endpoints da API
   Funcionalidade Método HTTP /URL Corpo (Exemplo JSON)    Resposta de Sucesso

   Criar Cliente POST /clientes { "nome": "...", "cpf": ..., "tipoConta": "Corrente"} (limite/taxa/rendimento opcionais)
   201 Created com o cliente criado

   Listar Clientes Ativos GET /clientes N/A 200 OK com a lista de clientes

   Buscar Cliente por ID GET /clientes/{id} N/A 200 OK com os dados do cliente

   Atualizar Cliente PUT /clientes/{id} { "nome": "Novo Nome" } 200 OK com os dados atualizados

   Desativar Cliente DELETE /clientes/{id} N/A 204 No Content

   Abrir Nova Conta POST /clientes/{clienteId}/contas { "tipoConta": "Poupanca", "saldoInicial": "50.00" } (
   limite/taxa/rendimento opcionais)    201 Created com a nova conta

   Depositar POST /contas/{numeroConta}/depositar { "valor": "100.00" } 200 OK com mensagem de sucesso

   Sacar POST /contas/{numeroConta}/sacar { "valor": "50.00" } 200 OK com mensagem de sucesso

   Transferir POST /contas/{numContaOrigem}/transferir { "numeroContaDestino": ..., "valor": "..." } 200 OK com mensagem
   de sucesso

   Consultar Extrato GET /contas/{numeroConta}/extrato N/A 200 OK com a lista de transações

   Atualizar C. Corrente PATCH /contas/corrente/{numeroConta} { "limite": 2000, "taxa": "0.01" } (campos opcionais)

5. Conclusão e Próximos Passos
   A API em seu estado atual é uma aplicação robusta, funcional e com código limpo, cobrindo os casos de uso essenciais
   de um sistema bancário. O próximo passo natural em sua evolução seria focar na Segurança (autenticação/autorização
   com Spring Security), seguida pela implementação de uma suíte de Testes Automatizados e melhorias operacionais como
   Paginação.