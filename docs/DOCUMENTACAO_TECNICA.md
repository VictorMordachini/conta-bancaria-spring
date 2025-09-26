Documentação Mestra da API de Conta Bancária
Versão: 1.0.0 (Estável)
Data: 26 de setembro de 2025

Índice
Visão Geral e Arquitetura

O mapa da nossa aplicação.

Dicionário de Tecnologias, Conceitos e Padrões

O "porquê" de cada ferramenta e decisão.

A. Spring Boot e Spring Framework

B. JPA / Hibernate (Persistência)

C. Java e Boas Práticas

D. Padrões de Projeto e Arquitetura

Detalhamento das Classes e Funções

Um tour guiado por todo o código.

A. Camada de Domínio (domain)

B. Camada de Aplicação (application)

C. Camada de Interface (interface_ui)

Conclusão e Próximos Passos

1. Visão Geral e Arquitetura
   A API foi projetada com uma arquitetura em camadas inspirada no Domain-Driven Design (DDD). Esta abordagem organiza o código de acordo com as responsabilidades de negócio, resultando em um sistema mais claro, flexível e fácil de manter.

domain (Domínio): O coração da aplicação. Contém toda a lógica de negócio e as regras que definem o que é uma conta bancária, um cliente, etc. É totalmente independente das outras camadas.

application (Aplicação): O orquestrador. Serve como uma camada intermediária que coordena os casos de uso (ex: "criar um novo cliente"), convertendo dados de entrada (DTOs) em ações no domínio.

interface_ui (Interface / Exposição): A fachada. É a camada que se comunica com o mundo exterior (neste caso, via HTTP/JSON), expondo a API através de endpoints REST e tratando os erros de forma centralizada.

2. Dicionário de Tecnologias, Conceitos e Padrões
   Esta seção explica os "porquês" por trás das ferramentas e decisões técnicas.

A. Spring Boot e Spring Framework
@RestController: Anotação que marca uma classe como um controlador web. Sua principal função é receber requisições HTTP e retornar respostas, que são automaticamente convertidas para JSON.

@RequestMapping, @GetMapping, @PostMapping, etc.: Mapeiam as URLs e os métodos HTTP (GET, POST, PUT, DELETE) para os métodos específicos nos nossos controllers, definindo os endpoints da API.

@Service: Marca uma classe como um componente da camada de serviço. O Spring a gerencia e permite que seja injetada em outras classes.

Injeção de Dependência: O coração do Spring. Em vez de uma classe criar suas próprias dependências, ela as declara no construtor. O Spring se encarrega de "injetar" as instâncias prontas. Por que usamos? Para criar um código desacoplado e fácil de testar.

@Transactional: Garante que um método seja executado dentro de uma transação de banco de dados. Por que usamos? É crítico para operações financeiras para garantir a consistência dos dados (propriedade de Atomicidade). Ou a operação inteira funciona, ou nada é alterado.

@ControllerAdvice e GlobalExceptionHandler: Transforma uma classe em um "conselheiro" global para os controllers. Por que usamos? Para centralizar todo o tratamento de erros em um único lugar, mantendo os controllers limpos e as respostas de erro da API consistentes.

@Valid e Jakarta Bean Validation: @Valid aciona a validação em um objeto de entrada (DTO). As anotações (@NotBlank, @Positive) definem as regras. Por que usamos? Para proteger a API de dados inválidos na sua "porta de entrada" e fornecer feedback claro ao cliente (400 Bad Request).

B. JPA / Hibernate (Persistência)
@Entity: Anotação do JPA que transforma uma classe Java comum em uma tabela no banco de dados.

@Inheritance(strategy = InheritanceType.SINGLE_TABLE): Define como a herança entre Conta e suas subclasses é representada no banco. Por que usamos? A estratégia SINGLE_TABLE cria uma única tabela para todos os tipos de conta, sendo simples e performática para consultas. Exige que colunas específicas de subclasses (limite, rendimento) permitam valores nulos.

@Version (Lock Otimista): Adiciona um campo de versionamento à entidade. Por que usamos? Para prevenir condições de corrida. Garante que operações simultâneas na mesma conta não corrompam os dados, fazendo a segunda operação falhar com um erro de conflito (409 Conflict).

C. Java e Boas Práticas
BigDecimal: Uma classe Java para cálculos com precisão decimal exata. Por que usamos? Tipos como double são imprecisos para finanças. BigDecimal é a única escolha correta para manipular dinheiro em Java.

enum (ex: TipoTransacao): Um tipo que representa um conjunto fixo de constantes. Por que usamos? Para segurança de tipo e legibilidade, evitando o uso de Strings que podem ser digitadas incorretamente.

@SuperBuilder (Lombok): Gera o código para o padrão de projeto "Builder", que permite construir objetos de forma fluida e legível (Objeto.builder().campo(valor).build()). Por que usamos? Para tornar a criação de entidades no serviço mais clara e menos propensa a erros.

D. Padrões de Projeto e Arquitetura
DTO (Data Transfer Object): Classes simples que carregam dados entre as camadas. Por que usamos? Para desacoplar a API do nosso modelo de domínio. O modelo interno (Conta, Cliente) pode mudar sem quebrar o "contrato" público da API, que é definido pelos DTOs.

Soft Delete: Em vez de apagar um registro do banco, o marcamos como inativo com um campo boolean ativo. Por que usamos? Em sistemas financeiros, nunca se deve apagar dados permanentemente, para preservar o histórico para auditoria e integridade.

3. Detalhamento das Classes e Funções
   Um tour guiado pelo código da aplicação.

A. Camada de Domínio (domain)
entity.Conta: Classe abstrata que serve como base para ContaCorrente e ContaPoupanca, definindo atributos comuns (saldo, numero, ativa) e o "contrato" de comportamento (abstract sacar).

entity.ContaCorrente / ContaPoupanca: Implementações concretas de Conta. Cada uma sobrescreve (@Override) o método sacar com suas regras específicas, demonstrando Polimorfismo.

entity.Cliente: Raiz de Agregação. Gerencia sua lista de Contas. O método adicionarConta garante que o relacionamento seja estabelecido corretamente.

entity.Transacao: Registro de uma movimentação financeira. Usa @PrePersist para gravar a data/hora automaticamente.

repository.ClienteRepository (e outros): Interfaces que estendem JpaRepository. Elas herdam métodos CRUD e permitem a criação de consultas customizadas a partir do nome do método (ex: findAllByAtivoTrue).

service.ContaServiceDomain: Serviço de Domínio que contém a lógica de transferir, pois essa operação complexa envolve duas entidades Conta e a criação de duas Transacao.

B. Camada de Aplicação (application)
dto.ClienteRequestDTO (e outros RequestDTOs): Modelam os dados recebidos pela API. Contêm anotações de validação.

dto.ClienteResponseDTO (e outros ResponseDTOs): Modelam os dados enviados pela API. Contêm o método fromEntity para a conversão de entidades.

service.ClienteService: Serviço de Aplicação que orquestra os casos de uso. Por exemplo, o método criarCliente recebe um ClienteRequestDTO, usa o Builder para criar as entidades Cliente e Conta, usa o repositório para salvá-las, registra a transação inicial e retorna um ClienteResponseDTO.

C. Camada de Interface (interface_ui)
controller.ClienteController / ContaController: Recebem as requisições HTTP. Seus métodos usam @PathVariable para pegar dados da URL e @RequestBody para pegar dados do corpo JSON. Delegam todo o trabalho para os serviços e empacotam a resposta em um ResponseEntity para controlar o status HTTP.

exception.GlobalExceptionHandler: Intercepta exceções de todos os controllers e formata respostas de erro JSON padronizadas para cada tipo de problema (Validação, Regra de Negócio, Conflito, Erro do Servidor).

4. Conclusão e Próximos Passos
   A API em seu estado atual é uma aplicação robusta, funcional e bem-estruturada, cobrindo os casos de uso essenciais de um sistema bancário. O próximo passo natural em sua evolução seria focar na Segurança (autenticação/autorização com Spring Security), seguida pela implementação de uma suíte de Testes Automatizados e melhorias operacionais como Paginação e Documentação com Swagger.