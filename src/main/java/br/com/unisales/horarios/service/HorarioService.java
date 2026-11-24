package br.com.unisales.horarios.service;

import br.com.unisales.horarios.dto.HorarioRequest;
import br.com.unisales.horarios.dto.HorarioResponse;
import br.com.unisales.horarios.exception.ConflitoHorarioException;
import br.com.unisales.horarios.exception.HorarioInvalidoException;
import br.com.unisales.horarios.exception.ResourceNotFoundException;
import br.com.unisales.horarios.model.Disciplina;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Professor;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.repository.DisciplinaRepository;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.ProfessorRepository;
import br.com.unisales.horarios.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HorarioService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final HorarioRepository horarioRepository;
    private final ProfessorRepository professorRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;

    public HorarioService(HorarioRepository horarioRepository,
                          ProfessorRepository professorRepository,
                          DisciplinaRepository disciplinaRepository,
                          TurmaRepository turmaRepository) {
        this.horarioRepository = horarioRepository;
        this.professorRepository = professorRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
    }

    public List<HorarioResponse> listar() {
        return horarioRepository.findAll().stream()
                .sorted(Horario.COMPARADOR_GRADE)
                .map(HorarioResponse::de)
                .toList();
    }

    public HorarioResponse buscarPorId(Long id) {
        return HorarioResponse.de(buscarEntidade(id));
    }

    @Transactional
    public HorarioResponse criar(HorarioRequest request) {
        validarIntervalo(request);

        Professor professor = buscarProfessor(request.professorId());
        Disciplina disciplina = buscarDisciplina(request.disciplinaId());
        Turma turma = buscarTurma(request.turmaId());

        validarConflitos(request, null);

        Horario horario = new Horario(
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim(),
                professor,
                disciplina,
                turma);
        return HorarioResponse.de(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioResponse atualizar(Long id, HorarioRequest request) {
        Horario horario = buscarEntidade(id);
        validarIntervalo(request);

        Professor professor = buscarProfessor(request.professorId());
        Disciplina disciplina = buscarDisciplina(request.disciplinaId());
        Turma turma = buscarTurma(request.turmaId());

        validarConflitos(request, id);

        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFim(request.horaFim());
        horario.setProfessor(professor);
        horario.setDisciplina(disciplina);
        horario.setTurma(turma);
        return HorarioResponse.de(horarioRepository.save(horario));
    }

    @Transactional
    public void excluir(Long id) {
        horarioRepository.delete(buscarEntidade(id));
    }

    private void validarIntervalo(HorarioRequest request) {
        if (!request.horaFim().isAfter(request.horaInicio())) {
            throw new HorarioInvalidoException(
                    "A hora de termino (" + request.horaFim().format(FORMATO_HORA)
                            + ") deve ser posterior a hora de inicio (" + request.horaInicio().format(FORMATO_HORA) + ").");
        }
    }

    /**
     * Verifica sobreposicao de horarios para o mesmo professor, turma e
     * disciplina no dia informado. Horarios adjacentes sao permitidos.
     * Na atualizacao, o proprio registro e ignorado na comparacao.
     */
    private void validarConflitos(HorarioRequest request, Long idAtual) {
        List<Horario> sobrepostos = horarioRepository.buscarSobrepostos(
                request.diaSemana(), request.horaInicio(), request.horaFim());

        for (Horario outro : sobrepostos) {
            if (idAtual != null && idAtual.equals(outro.getId())) {
                continue;
            }

            if (outro.getProfessor().getId().equals(request.professorId())) {
                throw new ConflitoHorarioException(
                        "Conflito de horario: o professor " + outro.getProfessor().getNome()
                                + " ja ministra " + outro.getDisciplina().getNome()
                                + " para a turma " + outro.getTurma().getIdentificacao()
                                + " na " + descrever(outro) + ".");
            }

            if (outro.getTurma().getId().equals(request.turmaId())) {
                throw new ConflitoHorarioException(
                        "Conflito de horario: a turma " + outro.getTurma().getIdentificacao()
                                + " ja possui " + outro.getDisciplina().getNome()
                                + " com o professor " + outro.getProfessor().getNome()
                                + " na " + descrever(outro) + ".");
            }

            if (outro.getDisciplina().getId().equals(request.disciplinaId())) {
                throw new ConflitoHorarioException(
                        "Conflito de horario: a disciplina " + outro.getDisciplina().getNome()
                                + " ja esta alocada na " + descrever(outro)
                                + " (professor " + outro.getProfessor().getNome()
                                + ", turma " + outro.getTurma().getIdentificacao() + ").");
            }
        }
    }

    private String descrever(Horario horario) {
        return horario.getDiaSemana().getDescricao()
                + " das " + formatar(horario.getHoraInicio())
                + " as " + formatar(horario.getHoraFim());
    }

    private String formatar(LocalTime hora) {
        return hora.format(FORMATO_HORA);
    }

    private Horario buscarEntidade(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario nao encontrado com id " + id + "."));
    }

    private Professor buscarProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor nao encontrado com id " + id + "."));
    }

    private Disciplina buscarDisciplina(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina nao encontrada com id " + id + "."));
    }

    private Turma buscarTurma(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id " + id + "."));
    }
}
