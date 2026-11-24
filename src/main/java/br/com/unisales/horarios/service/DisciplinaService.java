package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.DisciplinaRequest;
import br.com.unisales.horarios.dto.DisciplinaResponse;
import br.com.unisales.horarios.exception.RegraDeNegocioException;
import br.com.unisales.horarios.exception.ResourceNotFoundException;
import br.com.unisales.horarios.model.Disciplina;
import br.com.unisales.horarios.repository.DisciplinaRepository;
import br.com.unisales.horarios.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final HorarioRepository horarioRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, HorarioRepository horarioRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<DisciplinaResponse> listar() {
        return disciplinaRepository.findAllByOrderByNomeAsc().stream()
                .map(DisciplinaResponse::de)
                .toList();
    }

    public DisciplinaResponse buscarPorId(Long id) {
        return DisciplinaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest request) {
        Disciplina disciplina = new Disciplina(
                request.nome().trim(),
                request.descricao() == null ? null : request.descricao().trim(),
                request.cargaHoraria());
        return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public DisciplinaResponse atualizar(Long id, DisciplinaRequest request) {
        Disciplina disciplina = buscarEntidade(id);
        disciplina.setNome(request.nome().trim());
        disciplina.setDescricao(request.descricao() == null ? null : request.descricao().trim());
        disciplina.setCargaHoraria(request.cargaHoraria());
        return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = buscarEntidade(id);
        if (horarioRepository.existsByDisciplinaId(id)) {
            throw new RegraDeNegocioException(
                    "Nao e possivel excluir a disciplina '" + disciplina.getNome()
                            + "' pois existem horarios vinculados a ela. Exclua ou remaneje os horarios primeiro.");
        }
        disciplinaRepository.delete(disciplina);
    }

    private Disciplina buscarEntidade(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina nao encontrada com id " + id + "."));
    }
}
