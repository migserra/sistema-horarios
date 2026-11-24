package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.HorarioRequest;
import br.com.unisales.horarios.dto.HorarioResponse;
import br.com.unisales.horarios.exception.ConflitoHorarioException;
import br.com.unisales.horarios.exception.HorarioInvalidoException;
import br.com.unisales.horarios.exception.ResourceNotFoundException;
import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Disciplina;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Professor;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.repository.DisciplinaRepository;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.ProfessorRepository;
import br.com.unisales.horarios.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private HorarioService horarioService;

    private Professor professor;
    private Disciplina disciplina;
    private Turma turma;

    @BeforeEach
    void preparar() {
        professor = professor(1L, "Ana Souza");
        disciplina = disciplina(2L, "Banco de Dados");
        turma = turma(3L, "ES1");
    }

    @Test
    @DisplayName("deve criar horario quando as referencias existem e nao ha conflito")
    void deveCriarHorarioSemConflito() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);
        prepararReferenciasExistentes();
        when(horarioRepository.buscarSobrepostos(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40)))
                .thenReturn(List.of());
        when(horarioRepository.save(any(Horario.class))).thenAnswer(invocacao -> {
            Horario salvo = invocacao.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        HorarioResponse resposta = horarioService.criar(request);

        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.diaSemana()).isEqualTo(DiaSemana.SEGUNDA);
        assertThat(resposta.professor().nome()).isEqualTo("Ana Souza");
        assertThat(resposta.disciplina().nome()).isEqualTo("Banco de Dados");
        assertThat(resposta.turma().identificacao()).isEqualTo("ES1");
        verify(horarioRepository).save(any(Horario.class));
    }

    @Test
    @DisplayName("deve impedir horario com termino igual ao inicio")
    void deveImpedirHoraFimIgualAoInicio() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "19:00", 1L, 2L, 3L);

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(HorarioInvalidoException.class)
                .hasMessageContaining("posterior");

        verifyNoInteractions(horarioRepository, professorRepository, disciplinaRepository, turmaRepository);
    }

    @Test
    @DisplayName("deve impedir horario com termino anterior ao inicio")
    void deveImpedirHoraFimAnteriorAoInicio() {
        HorarioRequest request = request(DiaSemana.TERCA, "21:00", "20:00", 1L, 2L, 3L);

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(HorarioInvalidoException.class)
                .hasMessageContaining("posterior");

        verifyNoInteractions(horarioRepository);
    }

    @Test
    @DisplayName("deve impedir conflito do mesmo professor em horarios sobrepostos")
    void deveImpedirConflitoDeProfessor() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);
        prepararReferenciasExistentes();

        Horario existente = horario(99L, DiaSemana.SEGUNDA, "19:30", "20:30",
                professor, disciplina(20L, "Redes"), turma(30L, "CC1"));
        when(horarioRepository.buscarSobrepostos(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40)))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(ConflitoHorarioException.class)
                .hasMessageContaining("professor")
                .hasMessageContaining("ja ministra");

        verify(horarioRepository, never()).save(any(Horario.class));
    }

    @Test
    @DisplayName("deve impedir conflito da mesma turma em horarios sobrepostos")
    void deveImpedirConflitoDeTurma() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);
        prepararReferenciasExistentes();

        Horario existente = horario(99L, DiaSemana.SEGUNDA, "20:00", "21:00",
                professor(5L, "Carlos"), disciplina(20L, "Redes"), turma);
        when(horarioRepository.buscarSobrepostos(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40)))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(ConflitoHorarioException.class)
                .hasMessageContaining("turma")
                .hasMessageContaining("ja possui");

        verify(horarioRepository, never()).save(any(Horario.class));
    }

    @Test
    @DisplayName("deve impedir conflito da mesma disciplina em horarios sobrepostos")
    void deveImpedirConflitoDeDisciplina() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);
        prepararReferenciasExistentes();

        Horario existente = horario(99L, DiaSemana.SEGUNDA, "18:30", "19:30",
                professor(5L, "Carlos"), disciplina, turma(30L, "CC1"));
        when(horarioRepository.buscarSobrepostos(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40)))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(ConflitoHorarioException.class)
                .hasMessageContaining("disciplina")
                .hasMessageContaining("ja esta alocada");

        verify(horarioRepository, never()).save(any(Horario.class));
    }

    @Test
    @DisplayName("deve ignorar o proprio registro na atualizacao")
    void deveIgnorarProprioRegistroNaAtualizacao() {
        Horario existente = horario(10L, DiaSemana.SEGUNDA, "19:00", "20:40",
                professor, disciplina, turma);
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);

        when(horarioRepository.findById(10L)).thenReturn(Optional.of(existente));
        prepararReferenciasExistentes();
        when(horarioRepository.buscarSobrepostos(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40)))
                .thenReturn(List.of(existente));
        when(horarioRepository.save(existente)).thenReturn(existente);

        HorarioResponse resposta = horarioService.atualizar(10L, request);

        assertThat(resposta.id()).isEqualTo(10L);
        verify(horarioRepository).save(existente);
    }

    @Test
    @DisplayName("deve retornar 404 quando o professor informado nao existe")
    void deveRetornarErroQuandoProfessorNaoExiste() {
        HorarioRequest request = request(DiaSemana.SEGUNDA, "19:00", "20:40", 1L, 2L, 3L);
        when(professorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Professor nao encontrado");

        verifyNoInteractions(horarioRepository, disciplinaRepository, turmaRepository);
    }

    @Test
    @DisplayName("deve retornar 404 ao buscar horario inexistente")
    void deveRetornarErroAoBuscarHorarioInexistente() {
        when(horarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Horario nao encontrado");
    }

    private void prepararReferenciasExistentes() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(disciplinaRepository.findById(2L)).thenReturn(Optional.of(disciplina));
        when(turmaRepository.findById(3L)).thenReturn(Optional.of(turma));
    }

    private HorarioRequest request(DiaSemana dia, String inicio, String fim,
                                   Long professorId, Long disciplinaId, Long turmaId) {
        return new HorarioRequest(dia, LocalTime.parse(inicio), LocalTime.parse(fim),
                professorId, disciplinaId, turmaId);
    }

    private Professor professor(Long id, String nome) {
        Professor professor = new Professor(nome, nome.toLowerCase().replace(" ", "") + "@unisales.edu.br", "27999990000");
        professor.setId(id);
        return professor;
    }

    private Disciplina disciplina(Long id, String nome) {
        Disciplina disciplina = new Disciplina(nome, "Descricao", 80);
        disciplina.setId(id);
        return disciplina;
    }

    private Turma turma(Long id, String identificacao) {
        Turma turma = new Turma(identificacao, "Engenharia de Software", "2026/1");
        turma.setId(id);
        return turma;
    }

    private Horario horario(Long id, DiaSemana dia, String inicio, String fim,
                            Professor professor, Disciplina disciplina, Turma turma) {
        Horario horario = new Horario(dia, LocalTime.parse(inicio), LocalTime.parse(fim),
                professor, disciplina, turma);
        horario.setId(id);
        return horario;
    }
}
