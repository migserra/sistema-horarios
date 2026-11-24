# Sistema de Gestão de Horários de Professores: desenvolvimento de uma API REST com Spring Boot

**[Nome completo do aluno]**
Curso de Engenharia de Software — Unisales
[matrícula] — [e-mail institucional]

---

> **Documento-base para o artigo científico.** Este texto foi estruturado para, após a inserção das capturas de tela, do link real do repositório e da adequação às normas da Unisales, resultar em um artigo de 5 a 7 páginas. As indicações entre colchetes devem ser substituídas ou removidas na versão final. As orientações de formatação estão ao final do arquivo.

## Resumo

A organização dos horários de aula de professores é uma atividade recorrente em instituições de ensino e, quando realizada em planilhas, está sujeita a sobreposição de aulas e informações desatualizadas. Este trabalho descreve o desenvolvimento de uma API REST para gerenciar horários de professores, construída com Java 21, Spring Boot 3, Spring Data JPA, Spring Security e MySQL. A aplicação oferece CRUD de professores, disciplinas, turmas e horários, consultas de grade por professor e por turma, validação de campos e relacionamentos, bloqueio de horários inválidos, detecção de conflitos para o mesmo professor, turma ou disciplina e autenticação HTTP Basic com senhas em BCrypt. A documentação é disponibilizada no Swagger/OpenAPI. Como resultado, obteve-se uma aplicação backend funcional, organizada em camadas, com persistência real em MySQL e 32 testes automatizados aprovados, demonstrando a aplicação integrada de princípios de arquitetura, validação e segurança em um problema concreto de engenharia de software.

**Palavras-chave:** API REST. Spring Boot. Gestão de horários. Arquitetura em camadas. Segurança da informação.

## 1 Introdução

A alocação de horários em uma instituição de ensino envolve restrições simultâneas: disponibilidade dos professores, matriz curricular das turmas e quantidade de aulas de cada disciplina. Quando o controle é manual, a consistência depende de conferência humana, o que favorece erros e dificulta a consulta consolidada (SOMMERVILLE, 2019). Uma API REST centraliza essas regras em um único serviço, utilizado por diferentes clientes, evitando que cada interface replique a mesma lógica (FIELDING, 2000).

Este trabalho tem como objetivo geral desenvolver uma API REST para o gerenciamento de horários de aula de professores. Os objetivos específicos são: implementar o CRUD de professores, disciplinas, turmas e horários; disponibilizar as grades por professor e por turma; validar campos, formatos e relacionamentos; impedir conflitos de horário; proteger os endpoints com autenticação HTTP Basic e senhas criptografadas; documentar a API com Swagger/OpenAPI; e verificar o funcionamento com testes automatizados e execução contra MySQL real.

O texto está organizado em seis seções: referencial teórico (seção 2), materiais e métodos (seção 3), desenvolvimento (seção 4), resultados e discussão (seção 5) e conclusão (seção 6), seguidas das referências e do apêndice com o espaço reservado ao repositório.

## 2 Referencial teórico

### 2.1 APIs REST e o protocolo HTTP

No estilo arquitetural REST, recursos são identificados por URIs e manipulados por métodos HTTP padronizados: `GET` recupera, `POST` cria, `PUT` atualiza e `DELETE` remove (FIELDING, 2000). Os códigos de status comunicam o resultado, como `200`, `201`, `204`, `400`, `401`, `404` e `409`. Neste projeto, as representações são trocadas em JSON, e a separação entre cliente e servidor permite consumir a API pelo Swagger UI, por `curl` ou por uma futura interface web sem alterar as regras.

### 2.2 Spring Boot e arquitetura em camadas

O Spring Boot oferece autoconfiguração, starters e servidor embutido, reduzindo o esforço de configuração (SPRING, 2026). A aplicação adota uma adaptação do padrão MVC: *controllers* recebem as requisições, *services* concentram as regras e *repositories* acessam os dados. Essa divisão atende ao princípio da responsabilidade única e facilita testes e manutenção (FOWLER, 2002). DTOs separam a API das entidades JPA, e o Bean Validation expressa restrições de forma declarativa (`@NotBlank`, `@Email`, `@NotNull`, `@Positive`).

### 2.3 Persistência, segurança e documentação

A JPA é uma especificação de mapeamento objeto-relacional; o Hibernate a implementa e o Spring Data JPA reduz o código de acesso a dados (SPRING DATA JPA, 2026). As entidades do sistema são persistidas no MySQL, banco relacional amplamente utilizado (MYSQL, 2026). Na segurança, o HTTP Basic envia credenciais em Base64 e deve ser usado sobre HTTPS em produção. Como senhas não devem ser armazenadas em texto puro, adota-se o BCrypt, que aplica *salt* e múltiplas rodadas de hash (OWASP, 2026; SPRING SECURITY, 2026). A especificação OpenAPI, gerada pelo Springdoc, descreve endpoints e autenticação e alimenta o Swagger UI (SPRINGDOC, 2026). JUnit 5, Mockito e MockMvc apoiam os testes automatizados (JUNIT, 2026).

## 3 Materiais e métodos

### 3.1 Caracterização e ambiente

Trata-se de pesquisa aplicada, de objetivo exploratório e descritivo, com abordagem qualitativa e procedimento de estudo de caso com prototipação incremental: os requisitos foram convertidos em entidades, regras e endpoints, implementados e verificados em etapas. A Tabela 1 apresenta as tecnologias utilizadas; o ambiente de verificação foi o Windows 11, com JDK 21.0.13, Maven 3.9.16, Spring Boot 3.5.16 e MySQL 8.4.5.

**Tabela 1 – Tecnologias e versões**

| Categoria | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.5.16 |
| Build | Maven | 3.9.16 |
| Persistência | Spring Data JPA / Hibernate | gerenciadas pelo Spring Boot |
| Banco de dados | MySQL | 8.4.5 |
| Segurança | Spring Security / BCrypt | gerenciados pelo Spring Boot |
| Documentação | Springdoc OpenAPI / Swagger UI | 2.8.17 |
| Testes | JUnit 5, Mockito, MockMvc, H2 | gerenciados pelo Spring Boot |

Fonte: elaborado pelo autor (2026).

### 3.2 Arquitetura e modelo de dados

A aplicação está organizada nos pacotes `controller`, `service`, `repository`, `model`, `dto`, `config`, `exception` e `security`. O fluxo de uma requisição é: cliente → filtro de segurança → controller → service → repository → MySQL. O controller valida o corpo e devolve `ResponseEntity`; o service aplica regras e lança exceções específicas; o handler global converte exceções em JSON. A Figura 1 deve ilustrar esse fluxo.

**Figura 1 – Arquitetura em camadas da API**

[INSERIR DIAGRAMA DE CAMADAS OU CAPTURA DA ESTRUTURA DE PACOTES]

Fonte: elaborado pelo autor (2026).

O modelo possui cinco entidades: `Professor` (`id`, `nome`, `email`, `telefone`), `Disciplina` (`id`, `nome`, `descricao`, `cargaHoraria`), `Turma` (`id`, `identificacao`, `curso`, `periodo`), `Horario` (`id`, `diaSemana`, `horaInicio`, `horaFim` e chaves estrangeiras) e `Usuario` (`id`, `login`, `senha`, `papel`). Cada horário referencia exatamente um professor, uma disciplina e uma turma; esses registros podem aparecer em vários horários. E-mail de professor e identificação de turma são únicos. O dia é uma enumeração (`SEGUNDA` a `SABADO`) e os horários usam `LocalTime`.

### 3.3 Regras de negócio

As regras são: campos e formatos válidos; unicidade de e-mail e de identificação de turma; término estritamente posterior ao início; ausência de sobreposição para o mesmo professor, turma ou disciplina; permissão de horários adjacentes; bloqueio de exclusão com vínculos; e desconsideração do próprio registro na atualização. Há sobreposição quando `novoInicio < existenteFim` **e** `novoFim > existenteInicio`; essa condição permite que uma aula comece quando outra termina. Em seguida, verifica-se se o horário sobreposto compartilha professor, turma ou disciplina, gerando mensagem específica.

## 4 Desenvolvimento do sistema

### 4.1 Entidades, DTOs e validação

As entidades usam JPA, com relacionamentos `@ManyToOne` obrigatórios em `Horario`, garantindo no banco que nenhum horário exista sem os três vínculos. As respostas utilizam DTOs com resumos de professor, disciplina e turma, evitando ciclos de serialização. Os DTOs de entrada validam textos obrigatórios, e-mail, carga horária positiva e identificadores. O formato de hora é `HH:mm`, e o dia aceita maiúsculas ou minúsculas por meio de um `@JsonCreator` na enumeração.

### 4.2 Serviço de horários e conflitos

O `HorarioService` valida o intervalo, busca as entidades relacionadas (404 quando ausentes), consulta os horários sobrepostos e identifica o tipo de conflito. A mensagem informa dia, horário e envolvidos, por exemplo: “Conflito de horario: a turma ES1 ja possui Banco de Dados com o professor Carlos Pereira na Segunda-feira das 20:50 as 22:30.” Na atualização, o próprio ID é ignorado na comparação. Os serviços de professor, disciplina e turma verificam vínculos antes de excluir e impedem duplicidades. As grades são ordenadas pela ordem do dia e, no dia, pelo horário de início, com um comparador único na entidade `Horario`.

### 4.3 Segurança e tratamento de exceções

A configuração do Spring Security usa sessões sem estado, desativa CSRF e exige autenticação em todas as rotas, exceto a documentação e `/error`. Um `UserDetailsService` carrega o usuário da tabela `usuario`, e o hash BCrypt é comparado pelo `DaoAuthenticationProvider`. Na primeira execução, cria-se o usuário `admin` com senha `admin123` criptografada; a senha nunca é armazenada ou registrada em texto puro. Respostas 401 e 403 são devolvidas em JSON, e o 401 inclui o cabeçalho `WWW-Authenticate`.

O `GlobalExceptionHandler` padroniza os erros. A Tabela 2 resume as situações e os status.

**Tabela 2 – Exceções e status HTTP**

| Situação | Status | Exemplo de mensagem |
|---|---|---|
| Validação de DTO | 400 | “Um ou mais campos estao invalidos.” + mapa `campos` |
| Corpo mal formatado ou hora inválida | 400 | “Corpo da requisicao ausente ou mal formatado…” |
| Término anterior ou igual ao início | 400 | “A hora de termino (19:00) deve ser posterior…” |
| Recurso inexistente | 404 | “Professor nao encontrado com id 99.” |
| Conflito de professor, turma ou disciplina | 409 | Mensagem com dia, horário e envolvidos |
| Exclusão com vínculos ou duplicidade | 409 | “...possui horarios vinculados...” |
| Erro inesperado | 500 | “Ocorreu um erro inesperado…” |

Fonte: elaborado pelo autor (2026).

### 4.4 Documentação e testes

O `OpenApiConfig` declara título, versão, descrição, o esquema `basicAuth` e o requisito global de segurança; os controllers usam `@Tag`, `@Operation`, `@Parameter` e `@ApiResponse`. O Swagger UI fica em `/swagger-ui.html` e a especificação em `/v3/api-docs` (Figura 2).

**Figura 2 – Swagger UI com autenticação HTTP Basic**

[INSERIR CAPTURA DE TELA DO SWAGGER UI – BOTÃO AUTHORIZE E ENDPOINTS]

Fonte: elaborado pelo autor (2026).

A suíte possui 15 testes unitários (JUnit 5 + Mockito), cobrindo conflitos, intervalo inválido, atualização, e-mail duplicado, exclusões bloqueadas e ordenação das grades, e 17 testes de integração (`@SpringBootTest` + MockMvc + H2 em modo MySQL), cobrindo 401, autenticação, documentação pública, CRUD, grades, horários adjacentes, conflitos, validações, 404 e 409. Os testes de integração não exigem MySQL instalado; o MySQL é usado na execução real. Quando `SEED_DEMO=true` e as tabelas estão vazias, o inicializador cria três professores, quatro disciplinas, três turmas e seis horários sem conflitos, facilitando a apresentação.

## 5 Resultados e discussão

### 5.1 Verificação executada

O projeto foi compilado e empacotado com Maven, e a suíte automatizada obteve 32 testes aprovados, sem falhas ou erros. A API foi então iniciada contra um MySQL 8.4.5 real, com os dados de demonstração criados automaticamente. Um script executou 19 verificações HTTP, todas aprovadas, abrangendo autenticação, CRUD, grades, conflitos, validações e bloqueios de exclusão. Por fim, a aplicação foi reiniciada e os registros permaneceram disponíveis, confirmando a persistência. A Tabela 3 resume essas evidências.

**Tabela 3 – Resultados da verificação local**

| Verificação | Procedimento | Resultado observado |
|---|---|---|
| Compilação e empacotamento | `mvn clean package` | Sucesso |
| Testes automatizados | `mvn test` | 32 aprovados, 0 falhas |
| Execução com MySQL | API + MySQL 8.4.5 | Inicialização e seed concluídos |
| Endpoints e regras | 19 chamadas HTTP automatizadas | 19 aprovadas |
| Segurança | Acesso sem credenciais e com senha incorreta | 401 com `WWW-Authenticate` |
| Documentação | `/v3/api-docs` e `/swagger-ui.html` | 200 e redirecionamento para a UI |
| Persistência | Reinício da aplicação | Dados mantidos no MySQL |

Fonte: elaborado pelo autor (2026).

As Figuras 3 e 4 devem apresentar uma consulta de grade por turma e uma resposta de conflito, capturadas durante a execução com o Swagger UI autenticado.

**Figura 3 – Consulta da grade de horários por turma no Swagger UI**

[INSERIR CAPTURA DE TELA DA REQUISIÇÃO GET /api/turmas/{id}/horarios E DA RESPOSTA]

Fonte: elaborado pelo autor (2026).

**Figura 4 – Resposta de conflito de horário (HTTP 409)**

[INSERIR CAPTURA DE TELA DE UM CADASTRO CONFLITANTE E DA MENSAGEM RETORNADA]

Fonte: elaborado pelo autor (2026).

### 5.2 Discussão e limitações

A centralização das regras trouxe ganhos diretos: todos os clientes recebem as mesmas validações, e as mensagens de conflito identificam o motivo e os envolvidos, reduzindo a conferência manual. A separação em camadas permitiu testar as regras sem banco e o fluxo HTTP com H2, mantendo o MySQL na execução real. A documentação tornou a API autoexplicativa, o que é útil na apresentação acadêmica.

Como limitações, destacam-se: ausência de interface gráfica, prevista no escopo; falta de paginação e filtros; cadastro de usuários restrito ao seed inicial; uso de `ddl-auto=update` em vez de migrações; e a regra que impede a mesma disciplina em duas turmas no mesmo horário, que deve ser revista se a instituição permitir ofertas paralelas. Não foram realizados testes de carga ou desempenho; portanto, não se afirmam resultados dessa natureza. Ainda assim, os objetivos funcionais e não funcionais propostos foram cumpridos, demonstrando a integração de Spring Boot, JPA, Spring Security, Bean Validation, Springdoc e testes automatizados em um cenário realista.

## 6 Conclusão

Este trabalho apresentou uma API REST para a gestão de horários de professores, com CRUD completo, grades por professor e turma, validação de conflitos, autenticação HTTP Basic com BCrypt e documentação Swagger. A arquitetura em camadas manteve as regras isoladas e testáveis, e a verificação local confirmou os endpoints e a persistência em MySQL. Como trabalhos futuros, sugere-se: interface web; perfis e permissões distintos; migrações com Flyway; paginação e filtros; relatórios de carga horária; e empacotamento com Docker. Recomenda-se, ainda, a realização de testes de carga e de segurança mais abrangentes.

## Referências

FIELDING, R. T. **Architectural Styles and the Design of Network-based Software Architectures**. 2000. Tese (Doutorado em Ciência da Computação) – University of California, Irvine, 2000.

FOWLER, M. **Patterns of Enterprise Application Architecture**. Boston: Addison-Wesley, 2002.

JUNIT TEAM. **JUnit 5 User Guide**. [S. l.], 2026. Disponível em: https://junit.org/junit5/docs/current/user-guide/. Acesso em: 23 nov. 2026.

MYSQL. **MySQL 8.4 Reference Manual**. [S. l.], 2026. Disponível em: https://dev.mysql.com/doc/refman/8.4/en/. Acesso em: 23 nov. 2026.

OWASP. **Password Storage Cheat Sheet**. [S. l.], 2026. Disponível em: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html. Acesso em: 23 nov. 2026.

SOMMERVILLE, I. **Engenharia de Software**. 10. ed. São Paulo: Pearson, 2019.

SPRING. **Spring Boot Reference Documentation**. [S. l.], 2026. Disponível em: https://docs.spring.io/spring-boot/index.html. Acesso em: 23 nov. 2026.

SPRING DATA JPA. **Spring Data JPA Reference Documentation**. [S. l.], 2026. Disponível em: https://docs.spring.io/spring-data/jpa/reference/. Acesso em: 23 nov. 2026.

SPRING SECURITY. **Spring Security Reference**. [S. l.], 2026. Disponível em: https://docs.spring.io/spring-security/reference/. Acesso em: 23 nov. 2026.

SPRINGDOC. **Springdoc OpenAPI Documentation**. [S. l.], 2026. Disponível em: https://springdoc.org/. Acesso em: 23 nov. 2026.

## Apêndice A – Repositório do projeto

O código-fonte completo, incluindo este documento, o script de banco, o README e os testes, está disponível em:

**[INSERIR AQUI O LINK REAL DO REPOSITÓRIO NO GITHUB]**

> O projeto ainda não foi publicado no GitHub no momento da elaboração deste documento. Após criar o repositório público e enviar o código, substitua a linha acima pelo endereço real e confira se o README foi renderizado corretamente.

---

## Orientações para adequação ao Guia de Normalização da Unisales (remover na versão final)

1. **Formatação:** A4; margens 3 cm (superior/esquerda) e 2 cm (inferior/direita); Arial ou Times New Roman 12; espaçamento 1,5; texto justificado; recuo de 1,25 cm na primeira linha.
2. **Título e autoria:** título em negrito e centralizado; nome, curso, matrícula e e-mail conforme o modelo institucional.
3. **Resumo:** parágrafo único de 150 a 250 palavras e três a cinco palavras-chave.
4. **Seções:** numeração progressiva; citações no padrão autor-data (NBR 10520).
5. **Figuras e tabelas:** legendas superiores e fonte inferior; citar cada figura no texto antes de exibi-la; inserir as capturas indicadas.
6. **Referências:** ajustar à NBR 6023, em ordem alfabética, com alinhamento à esquerda.
7. **Extensão:** com quatro figuras e três tabelas, o texto deve ocupar de 5 a 7 páginas; remover estas orientações e os colchetes antes da exportação.
8. **PDF:** exportar com fontes incorporadas e conferir a numeração de páginas.
