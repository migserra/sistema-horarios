package br.com.unisales.horarios.repository;

import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByProfessorId(Long professorId);

    List<Horario> findByTurmaId(Long turmaId);

    boolean existsByProfessorId(Long professorId);

    boolean existsByDisciplinaId(Long disciplinaId);

    boolean existsByTurmaId(Long turmaId);

    /**
     * Retorna os horarios do dia que se sobrepoem ao intervalo informado.
     * A condicao inicio < outroFim e fim > outroInicio permite que uma aula
     * comece exatamente quando a anterior termina (horarios adjacentes).
     */
    @Query("""
            SELECT h FROM Horario h
            WHERE h.diaSemana = :diaSemana
              AND h.horaInicio < :horaFim
              AND h.horaFim > :horaInicio
            """)
    List<Horario> buscarSobrepostos(
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim);
}
