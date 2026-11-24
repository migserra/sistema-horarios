package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.GradeProfessorResponse;
import br.com.unisales.horarios.dto.ProfessorRequest;
import br.com.unisales.horarios.dto.ProfessorResponse;
import br.com.unisales.horarios.exception.RegraDeNegocioException;
import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Disciplina;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Professor;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.ProfessorRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private ProfessorService professorService;

    private Professor professor;

    @BeforeEach
    void preparar() {
        professor = new Professor("Ana Souza", "ana.souza@unisales.edu.br", "(27) 99999-0001");
        professor.setId(1L);
    }

    @Test
    @DisplayName("deve normalizar nome e e-mail ao criar professor")
    void deveNormalizarDadosAoCriar() {
        when(professorRepository.existsByEmail("ana.souza@unisales.edu.br")).thenReturn(false);
        when(professorRepository.save(any(Professor.class))).thenAnswer(invocacao -> {
            Professor salvo = invocacao.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        ProfessorResponse resposta = professorService.criar(
                new ProfessorRequest("  Ana Souza ", " ANA.SOUZA@UNISALES.EDU.BR ", " (27) 99999-0001 "));

        assertThat(resposta.nome()).isEqualTo("Ana Souza");
        assertThat(resposta.email()).isEqualTo("ana.souza@unisales.edu.br");
    }

    @Test
    @DisplayName("deve rejeitar e-mail duplicado")
    void deveRejeitarEmailDuplicado() {
        when(professorRepository.existsByEmail("ana.souza@unisales.edu.br")).thenReturn(true);

        assertThatThrownBy(() -> professorService.criar(
                new ProfessorRequest("Outra Pessoa", "ana.souza@unisales.edu.br", "(27) 98888-0000")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("e-mail");

        verify(professorRepository, never()).save(any(Professor.class));
    }

    @Test
    @DisplayName("deve bloquear exclusao de professor com horarios vinculados")
    void deveBloquearExclusaoComHorarios() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(horarioRepository.existsByProfessorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> professorService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("horarios vinculados");

        verify(professorRepository, never()).delete(any(Professor.class));
    }

    @Test
    @DisplayName("deve retornar a grade do professor ordenada por dia e hora")
    void deveRetornarGradeOrdenada() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));

        Disciplina disciplina = new Disciplina("Banco de Dados", "Descricao", 80);
        disciplina.setId(2L);
        Turma turma = new Turma("ES1", "Engenharia de Software", "2026/1");
        turma.setId(3L);

        Horario quarta = horario(30L, DiaSemana.QUARTA, "19:00", "20:40", disciplina, turma);
        Horario segundaTarde = horario(20L, DiaSemana.SEGUNDA, "20:50", "22:30", disciplina, turma);
        Horario segundaNoite = horario(10L, DiaSemana.SEGUNDA, "19:00", "20:40", disciplina, turma);
        when(horarioRepository.findByProfessorId(1L))
                .thenReturn(List.of(quarta, segundaTarde, segundaNoite));

        GradeProfessorResponse grade = professorService.buscarGrade(1L);

        assertThat(grade.professorNome()).isEqualTo("Ana Souza");
        assertThat(grade.totalAulas()).isEqualTo(3);
        assertThat(grade.aulas()).extracting(aula -> aula.horaInicio())
                .containsExactly(LocalTime.of(19, 0), LocalTime.of(20, 50), LocalTime.of(19, 0));
        assertThat(grade.aulas()).extracting(aula -> aula.diaSemana())
                .containsExactly(DiaSemana.SEGUNDA, DiaSemana.SEGUNDA, DiaSemana.QUARTA);
    }

    private Horario horario(Long id, DiaSemana dia, String inicio, String fim,
                            Disciplina disciplina, Turma turma) {
        Horario horario = new Horario(dia, LocalTime.parse(inicio), LocalTime.parse(fim),
                professor, disciplina, turma);
        horario.setId(id);
        return horario;
    }
}
