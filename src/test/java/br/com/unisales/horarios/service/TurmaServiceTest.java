package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.TurmaRequest;
import br.com.unisales.horarios.exception.RegraDeNegocioException;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.TurmaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private TurmaService turmaService;

    @Test
    @DisplayName("deve rejeitar identificacao de turma duplicada")
    void deveRejeitarIdentificacaoDuplicada() {
        when(turmaRepository.existsByIdentificacao("ES1")).thenReturn(true);

        assertThatThrownBy(() -> turmaService.criar(
                new TurmaRequest("ES1", "Engenharia de Software", "2026/1")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("identificacao");

        verify(turmaRepository, never()).save(any(Turma.class));
    }

    @Test
    @DisplayName("deve bloquear exclusao de turma com horarios vinculados")
    void deveBloquearExclusaoComHorarios() {
        Turma turma = new Turma("ES1", "Engenharia de Software", "2026/1");
        turma.setId(1L);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(horarioRepository.existsByTurmaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> turmaService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("horarios vinculados");

        verify(turmaRepository, never()).delete(any(Turma.class));
    }
}
