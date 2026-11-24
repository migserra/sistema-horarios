# Sistema de Gestão de Horários de Professores

API REST desenvolvida em **Java 21 + Spring Boot 3** para organizar e gerenciar os horários de aula de professores, com CRUD completo, validação de conflitos, autenticação HTTP Basic e documentação OpenAPI/Swagger. Projeto acadêmico do curso de Engenharia de Software da Unisales, sem interface gráfica: o consumo é feito diretamente por ferramentas como Swagger UI, Postman ou `curl`.

> **Status:** projeto implementado e verificado localmente. O link do repositório público ainda deve ser inserido na seção [Publicação no GitHub](#publicação-no-github).

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Modelo de dados](#modelo-de-dados)
- [Regras de negócio](#regras-de-negócio)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do MySQL](#configuração-do-mysql)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Como executar](#como-executar)
- [Documentação Swagger](#documentação-swagger)
- [Endpoints](#endpoints)
- [Exemplos de requisições](#exemplos-de-requisições)
- [Formato de erros](#formato-de-erros)
- [Testes](#testes)
- [Usuário e dados de demonstração](#usuário-e-dados-de-demonstração)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Decisões e limitações](#decisões-e-limitações)
- [Publicação no GitHub](#publicação-no-github)

---

## Funcionalidades

- CRUD completo de **professores**, **disciplinas**, **turmas** e **horários**.
- Listagem, consulta por ID, edição e exclusão de todos os recursos.
- **Grade de horários por professor** (`/api/professores/{id}/horarios`), com disciplinas, turmas, dias e horários.
- **Grade de horários por turma** (`/api/turmas/{id}/horarios`), com professores, disciplinas, dias e horários.
- Validação de campos obrigatórios, formato de e-mail, carga horária positiva e relacionamentos existentes.
- Bloqueio de horários com término anterior ou igual ao início.
- Bloqueio de conflitos de horário para o mesmo **professor**, a mesma **turma** e a mesma **disciplina**; horários adjacentes são permitidos.
- Exclusão bloqueada quando existem horários vinculados (professor, disciplina ou turma).
- Autenticação **HTTP Basic** com senha armazenada em **BCrypt**.
- Tratamento global de exceções com respostas JSON padronizadas e status HTTP adequados.
- Documentação interativa no **Swagger UI**.
- Testes unitários (JUnit 5 + Mockito) e de integração (MockMvc + H2 em modo MySQL).

## Tecnologias

| Tecnologia | Versão / Uso |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.5.16 |
| Maven | 3.9.x (Maven Wrapper incluído) |
| Spring Web | API REST com `@RestController` |
| Spring Data JPA + Hibernate | Persistência e consultas |
| Spring Security | HTTP Basic + BCrypt |
| MySQL | 8.4 LTS (banco de produção/desenvolvimento) |
| Springdoc OpenAPI + Swagger UI | 2.8.17 |
| Jakarta Bean Validation | Validação de DTOs |
| JUnit 5 + Mockito + MockMvc | Testes automatizados |
| H2 | Banco em memória usado somente nos testes |

## Arquitetura

O projeto segue o padrão **MVC em camadas**, com responsabilidades separadas:

```
Cliente (curl, Swagger UI, Postman)
                │
                ▼
┌─────────────────────────────────────────────┐
│ Spring Security – autenticação HTTP Basic   │
└─────────────────────────────────────────────┘
                │
                ▼
Controller  →  recebe HTTP, valida DTOs, devolve ResponseEntity
    │
    ▼
Service     →  regras de negócio (conflitos, grades, exclusões)
    │
    ▼
Repository  →  Spring Data JPA
    │
    ▼
MySQL
```

Elementos de apoio:

- **DTOs** separados da entidade (`record` para entrada e saída).
- **GlobalExceptionHandler** centraliza a conversão de exceções em respostas JSON.
- **OpenApiConfig** e anotações Springdoc documentam endpoints, parâmetros e segurança.
- **DataInitializer** cria o usuário administrador e, opcionalmente, dados de demonstração.
- Regras de negócio ficam exclusivamente na camada `service`; controllers apenas orquestram.

## Modelo de dados

```
Professor (1) ───< Horario >─── (1) Disciplina
                       │
                       └──────── (1) Turma
```

| Entidade | Campos |
|---|---|
| `Professor` | `id`, `nome`, `email` (único), `telefone` |
| `Disciplina` | `id`, `nome`, `descricao`, `cargaHoraria` |
| `Turma` | `id`, `identificacao` (única), `curso`, `periodo` |
| `Horario` | `id`, `diaSemana`, `horaInicio`, `horaFim`, `professor_id`, `disciplina_id`, `turma_id` |
| `Usuario` | `id`, `login` (único), `senha` (hash BCrypt), `papel` |

Cada horário está associado a exatamente um professor, uma disciplina e uma turma. As tabelas são criadas e atualizadas automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`); o script [database/criar-banco.sql](database/criar-banco.sql) cria apenas o banco e o usuário da aplicação.

## Regras de negócio

1. **Campos obrigatórios e formatos**: nome, e-mail e telefone do professor; nome e carga horária da disciplina; identificação, curso e período da turma; todos os campos do horário.
2. **E-mail válido** e único entre professores (normalizado para minúsculas).
3. **Identificação de turma única**.
4. **Horário válido**: `horaFim` deve ser estritamente posterior a `horaInicio`.
5. **Conflito de professor**: o mesmo professor não pode ter duas aulas sobrepostas no mesmo dia.
6. **Conflito de turma**: a mesma turma não pode ter duas aulas sobrepostas no mesmo dia.
7. **Conflito de disciplina**: neste projeto, uma disciplina não pode ser ofertada em duas turmas no mesmo intervalo (premissa de oferta única por horário).
8. **Horários adjacentes são permitidos**: um horário 19:00–20:40 não conflita com 20:40–22:10.
9. **Exclusões protegidas**: professor, disciplina ou turma com horários vinculados não podem ser excluídos (HTTP 409).
10. **Atualização**: o próprio registro é ignorado na verificação de conflitos.

A sobreposição é calculada por `inicio < outroFim AND fim > outroInicio`, consulta implementada em `HorarioRepository.buscarSobrepostos`.

## Pré-requisitos

- **JDK 21** instalado (`java -version`).
- **MySQL 8.x** em execução local (ou acessível por rede).
- **Maven 3.9+** — opcional, pois o projeto inclui o **Maven Wrapper** (`mvnw` / `mvnw.cmd`).
- Porta `8080` livre (ou defina `SERVER_PORT`).
- Conexão com a internet na primeira execução (o Maven/Wrapper baixa dependências).

## Configuração do MySQL

Crie o banco e o usuário da aplicação executando o script como administrador do MySQL:

```bash
mysql -u root -p < database/criar-banco.sql
```

O script executa, de forma equivalente a:

```sql
CREATE DATABASE IF NOT EXISTS horarios_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'horarios'@'localhost' IDENTIFIED BY 'horarios123';
GRANT ALL PRIVILEGES ON horarios_db.* TO 'horarios'@'localhost';
FLUSH PRIVILEGES;
```

> **Segurança:** `horarios123` é apenas a senha padrão de desenvolvimento. Em qualquer ambiente real, troque a senha e utilize variáveis de ambiente (ou um gerenciador de segredos). O script e o `.env.example` existem apenas para facilitar a execução acadêmica.

As tabelas **não** precisam ser criadas manualmente: na primeira inicialização o Hibernate cria `professor`, `disciplina`, `turma`, `horario` e `usuario`.

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/horarios_db?...` | URL JDBC do MySQL |
| `DB_USERNAME` | `horarios` | Usuário do banco |
| `DB_PASSWORD` | `horarios123` | Senha do banco |
| `ADMIN_USERNAME` | `admin` | Login do usuário criado na primeira execução |
| `ADMIN_PASSWORD` | `admin123` | Senha do usuário (armazenada com BCrypt) |
| `SEED_DEMO` | `true` | Cria dados de demonstração quando as tabelas estão vazias |
| `SERVER_PORT` | `8080` | Porta HTTP da API |

O arquivo [`.env.example`](.env.example) documenta essas variáveis. **O Spring Boot não lê `.env` automaticamente**: copie os valores para variáveis de ambiente do terminal, da configuração de execução da IDE ou edite diretamente [`src/main/resources/application.properties`](src/main/resources/application.properties). Ferramentas externas como Docker Compose ou `dotenv` também podem carregar o arquivo.

## Como executar

### Linux / macOS

```bash
export DB_URL="jdbc:mysql://localhost:3306/horarios_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo"
export DB_USERNAME="horarios"
export DB_PASSWORD="horarios123"
export SEED_DEMO="true"

./mvnw spring-boot:run
```

### Windows (PowerShell)

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/horarios_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo"
$env:DB_USERNAME="horarios"
$env:DB_PASSWORD="horarios123"
$env:SEED_DEMO="true"

.\mvnw.cmd spring-boot:run
```

### Windows (Prompt de Comando)

```bat
set DB_URL=jdbc:mysql://localhost:3306/horarios_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
set DB_USERNAME=horarios
set DB_PASSWORD=horarios123
mvnw.cmd spring-boot:run
```

### Executando o JAR

```bash
./mvnw clean package
java -jar target/sistema-horarios-1.0.0.jar
```

A API sobe em `http://localhost:8080`. Se o MySQL estiver em outra porta/host, ajuste `DB_URL`.

## Documentação Swagger

- Swagger UI: **http://localhost:8080/swagger-ui.html** (redireciona para `/swagger-ui/index.html`)
- OpenAPI JSON: **http://localhost:8080/v3/api-docs**
- Login padrão: usuário `admin`, senha `admin123` (o endpoint de documentação é público, mas para executar requisições clique em **Authorize** e informe essas credenciais).
- O esquema de autenticação `basicAuth` está declarado em [OpenApiConfig.java](src/main/java/br/com/unisales/horarios/config/OpenApiConfig.java).

## Endpoints

Todos os endpoints abaixo, exceto a documentação, exigem **HTTP Basic**.

| Método | Caminho | Descrição | Sucesso |
|---|---|---|---|
| GET | `/api/professores` | Lista professores | 200 |
| GET | `/api/professores/{id}` | Consulta professor | 200 |
| POST | `/api/professores` | Cadastra professor | 201 |
| PUT | `/api/professores/{id}` | Atualiza professor | 200 |
| DELETE | `/api/professores/{id}` | Exclui professor (bloqueado se houver horários) | 204 |
| GET | `/api/professores/{id}/horarios` | Grade do professor | 200 |
| GET | `/api/disciplinas` | Lista disciplinas | 200 |
| GET | `/api/disciplinas/{id}` | Consulta disciplina | 200 |
| POST | `/api/disciplinas` | Cadastra disciplina | 201 |
| PUT | `/api/disciplinas/{id}` | Atualiza disciplina | 200 |
| DELETE | `/api/disciplinas/{id}` | Exclui disciplina (bloqueado se houver horários) | 204 |
| GET | `/api/turmas` | Lista turmas | 200 |
| GET | `/api/turmas/{id}` | Consulta turma | 200 |
| POST | `/api/turmas` | Cadastra turma | 201 |
| PUT | `/api/turmas/{id}` | Atualiza turma | 200 |
| DELETE | `/api/turmas/{id}` | Exclui turma (bloqueado se houver horários) | 204 |
| GET | `/api/turmas/{id}/horarios` | Grade da turma | 200 |
| GET | `/api/horarios` | Lista horários ordenados por dia/hora | 200 |
| GET | `/api/horarios/{id}` | Consulta horário | 200 |
| POST | `/api/horarios` | Cadastra horário (valida conflitos) | 201 |
| PUT | `/api/horarios/{id}` | Atualiza horário (valida conflitos) | 200 |
| DELETE | `/api/horarios/{id}` | Exclui horário | 204 |

Códigos de erro: **400** validação/intervalo inválido, **401** sem autenticação, **403** acesso negado, **404** recurso inexistente, **409** conflito de horário ou violação de regra de negócio.

### Valores aceitos para `diaSemana`

`SEGUNDA`, `TERCA`, `QUARTA`, `QUINTA`, `SEXTA`, `SABADO` — também aceitos em minúsculas (ex.: `segunda`). Horários no formato `HH:mm` (ex.: `19:00`).

## Exemplos de requisições

> Substitua a porta se necessário. Nos exemplos, as credenciais padrão `admin:admin123` são usadas via `-u`.

### Listar professores

```bash
curl -u admin:admin123 http://localhost:8080/api/professores
```

### Criar professor

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/professores \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","email":"ana.souza@unisales.edu.br","telefone":"(27) 99999-0001"}'
```

Resposta `201 Created` com cabeçalho `Location: /api/professores/{id}`:

```json
{
  "id": 1,
  "nome": "Ana Souza",
  "email": "ana.souza@unisales.edu.br",
  "telefone": "(27) 99999-0001"
}
```

### Criar disciplina, turma e horário

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/disciplinas \
  -H "Content-Type: application/json" \
  -d '{"nome":"Banco de Dados","descricao":"Modelagem relacional e SQL","cargaHoraria":80}'

curl -u admin:admin123 -X POST http://localhost:8080/api/turmas \
  -H "Content-Type: application/json" \
  -d '{"identificacao":"ES1","curso":"Engenharia de Software","periodo":"2026/1"}'

curl -u admin:admin123 -X POST http://localhost:8080/api/horarios \
  -H "Content-Type: application/json" \
  -d '{"diaSemana":"SEGUNDA","horaInicio":"19:00","horaFim":"20:40","professorId":1,"disciplinaId":1,"turmaId":1}'
```

### Consultar a grade do professor e da turma

```bash
curl -u admin:admin123 http://localhost:8080/api/professores/1/horarios
curl -u admin:admin123 http://localhost:8080/api/turmas/1/horarios
```

Exemplo de resposta da grade da turma:

```json
{
  "turmaId": 1,
  "turmaIdentificacao": "ES1",
  "curso": "Engenharia de Software",
  "periodo": "2026/1",
  "totalAulas": 2,
  "aulas": [
    {
      "diaSemana": "SEGUNDA",
      "horaInicio": "19:00",
      "horaFim": "20:40",
      "disciplinaId": 1,
      "disciplinaNome": "Banco de Dados",
      "professorId": 1,
      "professorNome": "Ana Souza"
    }
  ]
}
```

### Exemplo de conflito de horário (409)

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/horarios \
  -H "Content-Type: application/json" \
  -d '{"diaSemana":"SEGUNDA","horaInicio":"20:00","horaFim":"21:00","professorId":1,"disciplinaId":2,"turmaId":2}'
```

```json
{
  "timestamp": "2026-11-23T21:45:12.123Z",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Conflito de horario: o professor Ana Souza ja ministra Banco de Dados para a turma ES1 na Segunda-feira das 19:00 as 20:40.",
  "caminho": "/api/horarios"
}
```

## Formato de erros

Todas as exceções tratadas retornam JSON no seguinte formato:

```json
{
  "timestamp": "2026-11-23T21:45:12.123Z",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Um ou mais campos estao invalidos.",
  "caminho": "/api/professores",
  "campos": {
    "nome": "O nome e obrigatorio",
    "email": "Informe um e-mail valido"
  }
}
```

O campo `campos` aparece apenas em erros de validação. Detalhes em [GlobalExceptionHandler.java](src/main/java/br/com/unisales/horarios/exception/GlobalExceptionHandler.java).

## Testes

```bash
./mvnw test
```

- **15 testes unitários** (`HorarioServiceTest`, `ProfessorServiceTest`, `TurmaServiceTest`) cobrem conflitos de professor/turma/disciplina, intervalo inválido, atualização, normalização de e-mail, exclusões bloqueadas e ordenação das grades.
- **17 testes de integração** (`HorariosApiIntegrationTest`) cobrem autenticação 401, documentação pública, CRUD, grades por professor/turma, conflitos, validações, 404/409 e persistência do fluxo HTTP com MockMvc.
- Os testes de integração usam **H2 em memória no modo MySQL** (`src/main/resources/application-test.properties`), portanto **não exigem MySQL instalado**.
- Relatórios: `target/surefire-reports/`.

Executados localmente durante o desenvolvimento: **32 testes, 0 falhas**, além de verificação ponta a ponta contra MySQL 8.4.5 (19 verificações HTTP, incluindo conflitos e persistência após reinício da aplicação).

## Usuário e dados de demonstração

Na primeira execução, o `DataInitializer` cria:

- Usuário **`admin`** com senha **`admin123`**, armazenada como hash BCrypt (`$2a$10$...`). A senha só é criada se o usuário não existir; para alterá-la depois, defina `ADMIN_PASSWORD` antes da primeira execução ou remova o registro da tabela `usuario`.
- Quando `SEED_DEMO=true` e não há professores cadastrados: 3 professores, 4 disciplinas, 3 turmas (ES1, ES2, CC1) e 6 horários de exemplo, sem conflitos.
- Para começar com o banco vazio, use `SEED_DEMO=false`.

## Estrutura do projeto

```
.
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/
├── database/
│   └── criar-banco.sql
├── .env.example
├── .gitignore
├── src/main/java/br/com/unisales/horarios/
│   ├── SistemaHorariosApplication.java
│   ├── config/          OpenApiConfig, DataInitializer
│   ├── controller/      Professor, Disciplina, Turma, Horario
│   ├── dto/             requests, responses, resumos e grades
│   ├── exception/       exceções de domínio + handler global
│   ├── model/           Professor, Disciplina, Turma, Horario, DiaSemana, Usuario
│   ├── repository/      interfaces Spring Data JPA
│   ├── security/        SecurityConfig, UserDetailsService, handlers 401/403
│   └── service/         regras de negócio
├── src/main/resources/
│   ├── application.properties
│   └── application-test.properties
├── src/test/java/...    testes unitários e de integração
└── docs/
    └── artigo-cientifico.md
```

## Decisões e limitações

- **Sem frontend**: o projeto é exclusivamente backend/API REST, conforme o escopo acadêmico.
- **Banco**: MySQL com `ddl-auto=update` para simplificar a apresentação. Em produção, recomenda-se migrações versionadas (Flyway/Liquibase).
- **Autenticação**: usuário único criado por seed e papéis (`ADMIN`), sem endpoint de cadastro de usuários, para manter o foco nas regras de horários.
- **Conflito de disciplina**: a regra adicional impede a mesma disciplina em duas turmas no mesmo horário; se a instituição permitir ofertas paralelas, ela deve ser removida/parametrizada em `HorarioService.validarConflitos`.
- **Paginação e filtros**: não implementados por simplicidade acadêmica.
- **CORS**: não configurado, pois não há frontend consumindo a API.
- **Testes de carga/desempenho**: não realizados; o artigo não apresenta resultados desse tipo.

## Publicação no GitHub

O projeto ainda **não foi publicado** por este assistente. Para publicar:

```bash
git init
git add .
git commit -m "API REST para gestão de horários de professores"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/sistema-horarios.git
git push -u origin main
```

Antes de publicar, confirme:

- `.env` **não** está versionado (o `.gitignore` já o ignora);
- credenciais padrão são apenas para fins acadêmicos;
- o link real do repositório foi inserido abaixo e no artigo.

> **Link do repositório:** `[INSERIR AQUI O LINK DO REPOSITÓRIO NO GITHUB]`

---

Projeto acadêmico desenvolvido para o curso de **Engenharia de Software — Unisales**. Uso livre para fins de estudo, com atribuição.
