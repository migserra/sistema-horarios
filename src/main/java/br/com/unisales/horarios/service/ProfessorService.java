package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.AulaProfessorResponse;
import br.com.unisales.horarios.dto.GradeProfessorResponse;
import br.com.unisales.horarios.dto.ProfessorRequest;
import br.com.unisales.horarios.dto.ProfessorResponse;
import br.com.unisales.horarios.exception.RegraDeNegocioException;
import br.com.unisales.horarios.exception.ResourceNotFoundException;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Professor;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final HorarioRepository horarioRepository;

    public ProfessorService(ProfessorRepository professorRepository, HorarioRepository horarioRepository) {
        this.professorRepository = professorRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<ProfessorResponse> listar() {
        return professorRepository.findAllByOrderByNomeAsc().stream()
                .map(ProfessorResponse::de)
                .toList();
    }

    public ProfessorResponse buscarPorId(Long id) {
        return ProfessorResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ProfessorResponse criar(ProfessorRequest request) {
        String email = normalizarEmail(request.email());
        if (professorRepository.existsByEmail(email)) {
            throw new RegraDeNegocioException("Ja existe um professor cadastrado com o e-mail " + email + ".");
        }

        Professor professor = new Professor(
                request.nome().trim(),
                email,
                request.telefone().trim());
        return ProfessorResponse.de(professorRepository.save(professor));
    }

    @Transactional
    public ProfessorResponse atualizar(Long id, ProfessorRequest request) {
        Professor professor = buscarEntidade(id);
        String email = normalizarEmail(request.email());

        if (professorRepository.existsByEmailAndIdNot(email, id)) {
            throw new RegraDeNegocioException("Ja existe outro professor cadastrado com o e-mail " + email + ".");
        }

        professor.setNome(request.nome().trim());
        professor.setEmail(email);
        professor.setTelefone(request.telefone().trim());
        return ProfessorResponse.de(professorRepository.save(professor));
    }

    @Transactional
    public void excluir(Long id) {
        Professor professor = buscarEntidade(id);
        if (horarioRepository.existsByProfessorId(id)) {
            throw new RegraDeNegocioException(
                    "Nao e possivel excluir o professor '" + professor.getNome()
                            + "' pois existem horarios vinculados a ele. Exclua ou remaneje os horarios primeiro.");
        }
        professorRepository.delete(professor);
    }

    /**
     * Retorna a grade do professor com disciplinas e turmas de cada aula,
     * ordenada pelo dia da semana e pelo horario de inicio.
     */
    public GradeProfessorResponse buscarGrade(Long id) {
        Professor professor = buscarEntidade(id);
        List<AulaProfessorResponse> aulas = horarioRepository.findByProfessorId(id).stream()
                .sorted(Horario.COMPARADOR_GRADE)
                .map(AulaProfessorResponse::de)
                .toList();

        return new GradeProfessorResponse(professor.getId(), professor.getNome(), aulas.size(), aulas);
    }

    private Professor buscarEntidade(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor nao encontrado com id " + id + "."));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
