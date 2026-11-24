package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.AulaTurmaResponse;
import br.com.unisales.horarios.dto.GradeTurmaResponse;
import br.com.unisales.horarios.dto.TurmaRequest;
import br.com.unisales.horarios.dto.TurmaResponse;
import br.com.unisales.horarios.exception.RegraDeNegocioException;
import br.com.unisales.horarios.exception.ResourceNotFoundException;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final HorarioRepository horarioRepository;

    public TurmaService(TurmaRepository turmaRepository, HorarioRepository horarioRepository) {
        this.turmaRepository = turmaRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<TurmaResponse> listar() {
        return turmaRepository.findAllByOrderByIdentificacaoAsc().stream()
                .map(TurmaResponse::de)
                .toList();
    }

    public TurmaResponse buscarPorId(Long id) {
        return TurmaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest request) {
        String identificacao = request.identificacao().trim();
        if (turmaRepository.existsByIdentificacao(identificacao)) {
            throw new RegraDeNegocioException("Ja existe uma turma cadastrada com a identificacao '" + identificacao + "'.");
        }

        Turma turma = new Turma(identificacao, request.curso().trim(), request.periodo().trim());
        return TurmaResponse.de(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaResponse atualizar(Long id, TurmaRequest request) {
        Turma turma = buscarEntidade(id);
        String identificacao = request.identificacao().trim();

        if (turmaRepository.existsByIdentificacaoAndIdNot(identificacao, id)) {
            throw new RegraDeNegocioException("Ja existe outra turma cadastrada com a identificacao '" + identificacao + "'.");
        }

        turma.setIdentificacao(identificacao);
        turma.setCurso(request.curso().trim());
        turma.setPeriodo(request.periodo().trim());
        return TurmaResponse.de(turmaRepository.save(turma));
    }

    @Transactional
    public void excluir(Long id) {
        Turma turma = buscarEntidade(id);
        if (horarioRepository.existsByTurmaId(id)) {
            throw new RegraDeNegocioException(
                    "Nao e possivel excluir a turma '" + turma.getIdentificacao()
                            + "' pois existem horarios vinculados a ela. Exclua ou remaneje os horarios primeiro.");
        }
        turmaRepository.delete(turma);
    }

    /**
     * Retorna a grade da turma com professores e disciplinas de cada aula,
     * ordenada pelo dia da semana e pelo horario de inicio.
     */
    public GradeTurmaResponse buscarGrade(Long id) {
        Turma turma = buscarEntidade(id);
        List<AulaTurmaResponse> aulas = horarioRepository.findByTurmaId(id).stream()
                .sorted(Horario.COMPARADOR_GRADE)
                .map(AulaTurmaResponse::de)
                .toList();

        return new GradeTurmaResponse(
                turma.getId(),
                turma.getIdentificacao(),
                turma.getCurso(),
                turma.getPeriodo(),
                aulas.size(),
                aulas);
    }

    private Turma buscarEntidade(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id " + id + "."));
    }
}
