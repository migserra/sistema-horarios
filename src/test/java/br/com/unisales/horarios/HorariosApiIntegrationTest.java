package br.com.unisales.horarios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HorariosApiIntegrationTest {

    private static final String USUARIO = "admin";
    private static final String SENHA = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("deve retornar 401 sem credenciais e 401 com senha invalida")
    void deveExigirAutenticacaoBasica() throws Exception {
        mockMvc.perform(get("/api/professores"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")));

        mockMvc.perform(get("/api/professores").with(httpBasic(USUARIO, "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve autenticar com o usuario criado e BCrypt")
    void deveAutenticarComUsuarioPadrao() throws Exception {
        mockMvc.perform(get("/api/professores").with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve disponibilizar Swagger/OpenAPI sem autenticacao")
    void deveExporDocumentacaoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/api/professores']").exists())
                .andExpect(jsonPath("$.paths['/api/horarios']").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("deve executar o CRUD completo de professor")
    void deveExecutarCrudDeProfessor() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.crud@unisales.edu.br");

        mockMvc.perform(get("/api/professores/{id}", professorId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.telefone").value("(27) 99999-0000"));

        mockMvc.perform(get("/api/professores").with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + professorId + ")]", hasSize(1)));

        mockMvc.perform(put("/api/professores/{id}", professorId)
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Souza Lima","email":"ana.crud@unisales.edu.br","telefone":"(27) 98888-0000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.telefone").value("(27) 98888-0000"));

        mockMvc.perform(delete("/api/professores/{id}", professorId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/professores/{id}", professorId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("deve executar atualizacao e exclusao de disciplina e turma")
    void deveExecutarCrudDeDisciplinaETurma() throws Exception {
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        mockMvc.perform(put("/api/disciplinas/{id}", disciplinaId)
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Banco de Dados Avancado","descricao":"Tuning e transacoes","cargaHoraria":60}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Banco de Dados Avancado"))
                .andExpect(jsonPath("$.cargaHoraria").value(60));

        mockMvc.perform(put("/api/turmas/{id}", turmaId)
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificacao":"ES1-2026","curso":"Engenharia de Software","periodo":"2026/2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("ES1-2026"))
                .andExpect(jsonPath("$.periodo").value("2026/2"));

        mockMvc.perform(delete("/api/disciplinas/{id}", disciplinaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/turmas/{id}", turmaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/disciplinas/{id}", disciplinaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/turmas/{id}", turmaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deve criar horarios e consultar as grades do professor e da turma")
    void deveConsultarGradesPorProfessorETurma() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.grade@unisales.edu.br");
        Long outraProfessoraId = criarProfessor("Mariana Lima", "mariana.grade@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long outraDisciplinaId = criarDisciplina("Algoritmos", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/horarios/")))
                .andExpect(jsonPath("$.professor.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.disciplina.nome").value("Banco de Dados"))
                .andExpect(jsonPath("$.turma.identificacao").value("ES1"));

        criarHorario("SEGUNDA", "20:40", "22:10", outraProfessoraId, outraDisciplinaId, turmaId)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/professores/{id}/horarios", professorId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professorNome").value("Ana Souza"))
                .andExpect(jsonPath("$.totalAulas").value(1))
                .andExpect(jsonPath("$.aulas[0].diaSemana").value("SEGUNDA"))
                .andExpect(jsonPath("$.aulas[0].horaInicio").value("19:00"))
                .andExpect(jsonPath("$.aulas[0].horaFim").value("20:40"))
                .andExpect(jsonPath("$.aulas[0].disciplinaNome").value("Banco de Dados"))
                .andExpect(jsonPath("$.aulas[0].turmaIdentificacao").value("ES1"));

        mockMvc.perform(get("/api/turmas/{id}/horarios", turmaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turmaIdentificacao").value("ES1"))
                .andExpect(jsonPath("$.totalAulas").value(2))
                .andExpect(jsonPath("$.aulas[0].professorNome").value("Ana Souza"))
                .andExpect(jsonPath("$.aulas[1].professorNome").value("Mariana Lima"));
    }

    @Test
    @DisplayName("deve permitir horarios adjacentes para o mesmo professor e turma")
    void devePermitirHorariosAdjacentes() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.adjacente@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long outraDisciplinaId = criarDisciplina("Algoritmos", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("TERCA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());
        criarHorario("TERCA", "20:40", "22:10", professorId, outraDisciplinaId, turmaId)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("deve impedir conflito de horario do mesmo professor")
    void deveImpedirConflitoDeProfessor() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.conflito@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long outraDisciplinaId = criarDisciplina("Algoritmos", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");
        Long outraTurmaId = criarTurma("ES2", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());

        criarHorario("SEGUNDA", "20:00", "21:00", professorId, outraDisciplinaId, outraTurmaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem", containsString("professor")));
    }

    @Test
    @DisplayName("deve impedir conflito de horario da mesma turma")
    void deveImpedirConflitoDeTurma() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.turma@unisales.edu.br");
        Long outroProfessorId = criarProfessor("Carlos Pereira", "carlos.turma@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long outraDisciplinaId = criarDisciplina("Algoritmos", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());

        criarHorario("SEGUNDA", "20:00", "21:00", outroProfessorId, outraDisciplinaId, turmaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("turma")));
    }

    @Test
    @DisplayName("deve impedir conflito de horario da mesma disciplina")
    void deveImpedirConflitoDeDisciplina() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.disciplina@unisales.edu.br");
        Long outroProfessorId = criarProfessor("Carlos Pereira", "carlos.disciplina@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");
        Long outraTurmaId = criarTurma("ES2", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());

        criarHorario("SEGUNDA", "20:00", "21:00", outroProfessorId, disciplinaId, outraTurmaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("disciplina")));
    }

    @Test
    @DisplayName("deve impedir horario com termino anterior ou igual ao inicio")
    void deveImpedirIntervaloInvalido() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.intervalo@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "20:40", "19:00", professorId, disciplinaId, turmaId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("posterior")));

        criarHorario("SEGUNDA", "19:00", "19:00", professorId, disciplinaId, turmaId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("posterior")));
    }

    @Test
    @DisplayName("deve validar campos obrigatorios e formato de e-mail")
    void deveValidarCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/professores")
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"email-invalido","telefone":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.telefone").exists());
    }

    @Test
    @DisplayName("deve retornar 404 quando a referencia do horario nao existe")
    void deveRetornar404ParaReferenciaInexistente() throws Exception {
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", 99999L, disciplinaId, turmaId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", containsString("Professor nao encontrado")));
    }

    @Test
    @DisplayName("deve bloquear a exclusao de registros com horarios vinculados")
    void deveBloquearExclusaoComHorariosVinculados() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.vinculo@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/professores/{id}", professorId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("horarios vinculados")));
        mockMvc.perform(delete("/api/disciplinas/{id}", disciplinaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("horarios vinculados")));
        mockMvc.perform(delete("/api/turmas/{id}", turmaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("horarios vinculados")));
    }

    @Test
    @DisplayName("deve atualizar horario ignorando o proprio registro e detectando novos conflitos")
    void deveAtualizarHorario() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.atualiza@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long outraDisciplinaId = criarDisciplina("Algoritmos", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");
        Long outraTurmaId = criarTurma("ES2", "Engenharia de Software", "2026/1");

        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());
        Long horarioId = extrairId(criarHorario("SEGUNDA", "21:00", "22:30", professorId, outraDisciplinaId, outraTurmaId)
                .andExpect(status().isCreated()));

        atualizarHorario(horarioId, "SEGUNDA", "19:30", "20:50", professorId, outraDisciplinaId, outraTurmaId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("professor")));

        atualizarHorario(horarioId, "QUARTA", "19:00", "20:40", professorId, outraDisciplinaId, outraTurmaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("QUARTA"))
                .andExpect(jsonPath("$.horaInicio").value("19:00"));
    }

    @Test
    @DisplayName("deve aceitar o dia da semana em minusculas e excluir horario")
    void deveAceitarEnumMinusculoEExcluirHorario() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.enum@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");

        Long horarioId = extrairId(criarHorario("segunda", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaSemana").value("SEGUNDA")));

        mockMvc.perform(get("/api/horarios/{id}", horarioId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turma.identificacao").value("ES1"));

        mockMvc.perform(delete("/api/horarios/{id}", horarioId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/horarios/{id}", horarioId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/turmas/{id}/horarios", turmaId).with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAulas").value(0))
                .andExpect(jsonPath("$.aulas", hasSize(0)));
    }

    @Test
    @DisplayName("deve listar todos os horarios ordenados por dia e hora")
    void deveListarHorariosOrdenados() throws Exception {
        Long professorId = criarProfessor("Ana Souza", "ana.lista@unisales.edu.br");
        Long disciplinaId = criarDisciplina("Banco de Dados", 80);
        Long turmaId = criarTurma("ES1", "Engenharia de Software", "2026/1");
        Long outraTurmaId = criarTurma("ES2", "Engenharia de Software", "2026/1");

        criarHorario("QUARTA", "19:00", "20:40", professorId, disciplinaId, outraTurmaId)
                .andExpect(status().isCreated());
        criarHorario("SEGUNDA", "19:00", "20:40", professorId, disciplinaId, turmaId)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/horarios").with(httpBasic(USUARIO, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].diaSemana").value("SEGUNDA"))
                .andExpect(jsonPath("$[1].diaSemana").value("QUARTA"));
    }

    private Long criarProfessor(String nome, String email) throws Exception {
        ResultActions resultado = mockMvc.perform(post("/api/professores")
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","email":"%s","telefone":"(27) 99999-0000"}
                                """.formatted(nome, email)))
                .andExpect(status().isCreated());
        return extrairId(resultado);
    }

    private Long criarDisciplina(String nome, int cargaHoraria) throws Exception {
        ResultActions resultado = mockMvc.perform(post("/api/disciplinas")
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","descricao":"Disciplina de teste","cargaHoraria":%d}
                                """.formatted(nome, cargaHoraria)))
                .andExpect(status().isCreated());
        return extrairId(resultado);
    }

    private Long criarTurma(String identificacao, String curso, String periodo) throws Exception {
        ResultActions resultado = mockMvc.perform(post("/api/turmas")
                        .with(httpBasic(USUARIO, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificacao":"%s","curso":"%s","periodo":"%s"}
                                """.formatted(identificacao, curso, periodo)))
                .andExpect(status().isCreated());
        return extrairId(resultado);
    }

    private ResultActions criarHorario(String dia, String inicio, String fim,
                                       Long professorId, Long disciplinaId, Long turmaId) throws Exception {
        return mockMvc.perform(post("/api/horarios")
                .with(httpBasic(USUARIO, SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoHorario(dia, inicio, fim, professorId, disciplinaId, turmaId)));
    }

    private ResultActions atualizarHorario(Long horarioId, String dia, String inicio, String fim,
                                           Long professorId, Long disciplinaId, Long turmaId) throws Exception {
        return mockMvc.perform(put("/api/horarios/{id}", horarioId)
                .with(httpBasic(USUARIO, SENHA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoHorario(dia, inicio, fim, professorId, disciplinaId, turmaId)));
    }

    private String corpoHorario(String dia, String inicio, String fim,
                                Long professorId, Long disciplinaId, Long turmaId) {
        return """
                {"diaSemana":"%s","horaInicio":"%s","horaFim":"%s","professorId":%d,"disciplinaId":%d,"turmaId":%d}
                """.formatted(dia, inicio, fim, professorId, disciplinaId, turmaId);
    }

    private Long extrairId(ResultActions resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.andReturn().getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
