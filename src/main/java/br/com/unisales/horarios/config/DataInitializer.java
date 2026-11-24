package br.com.unisales.horarios.config;

import br.com.unisales.horarios.model.DiaSemana;
import br.com.unisales.horarios.model.Disciplina;
import br.com.unisales.horarios.model.Horario;
import br.com.unisales.horarios.model.Professor;
import br.com.unisales.horarios.model.Turma;
import br.com.unisales.horarios.model.Usuario;
import br.com.unisales.horarios.repository.DisciplinaRepository;
import br.com.unisales.horarios.repository.HorarioRepository;
import br.com.unisales.horarios.repository.ProfessorRepository;
import br.com.unisales.horarios.repository.TurmaRepository;
import br.com.unisales.horarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * Cria o usuario administrador na primeira execucao e, opcionalmente,
 * dados de demonstracao quando as tabelas estao vazias.
 * A senha do usuario nunca e armazenada em texto puro: usa-se BCrypt.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final ProfessorRepository professorRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final HorarioRepository horarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-login:admin}")
    private String adminLogin;

    @Value("${app.seed.admin-senha:admin123}")
    private String adminSenha;

    @Value("${app.seed.dados-demo:true}")
    private boolean criarDadosDemo;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ProfessorRepository professorRepository,
                           DisciplinaRepository disciplinaRepository,
                           TurmaRepository turmaRepository,
                           HorarioRepository horarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.professorRepository = professorRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.horarioRepository = horarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        criarUsuarioAdministrador();
        if (criarDadosDemo) {
            criarDadosDemonstracao();
        }
    }

    private void criarUsuarioAdministrador() {
        if (usuarioRepository.existsByLogin(adminLogin)) {
            return;
        }
        usuarioRepository.save(new Usuario(adminLogin, passwordEncoder.encode(adminSenha), "ADMIN"));
        log.info("Usuario administrador '{}' criado com senha criptografada via BCrypt.", adminLogin);
    }

    private void criarDadosDemonstracao() {
        if (professorRepository.count() > 0) {
            return;
        }

        Professor ana = professorRepository.save(
                new Professor("Ana Souza", "ana.souza@unisales.edu.br", "(27) 99999-0001"));
        Professor carlos = professorRepository.save(
                new Professor("Carlos Pereira", "carlos.pereira@unisales.edu.br", "(27) 99999-0002"));
        Professor mariana = professorRepository.save(
                new Professor("Mariana Lima", "mariana.lima@unisales.edu.br", "(27) 99999-0003"));

        Disciplina algoritmos = disciplinaRepository.save(new Disciplina(
                "Algoritmos e Programação", "Lógica, estruturas de controle e análise de complexidade.", 80));
        Disciplina bancoDados = disciplinaRepository.save(new Disciplina(
                "Banco de Dados", "Modelagem relacional, SQL e transações.", 80));
        Disciplina engenhariaSoftware = disciplinaRepository.save(new Disciplina(
                "Engenharia de Software", "Processos, requisitos e qualidade de software.", 60));
        Disciplina redes = disciplinaRepository.save(new Disciplina(
                "Redes de Computadores", "Protocolos TCP/IP, roteamento e segurança de redes.", 60));

        Turma es1 = turmaRepository.save(new Turma("ES1", "Engenharia de Software", "2026/1"));
        Turma es2 = turmaRepository.save(new Turma("ES2", "Engenharia de Software", "2026/1"));
        Turma cc1 = turmaRepository.save(new Turma("CC1", "Ciência da Computação", "2026/1"));

        horarioRepository.saveAll(List.of(
                new Horario(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40),
                        ana, algoritmos, es1),
                new Horario(DiaSemana.SEGUNDA, LocalTime.of(20, 50), LocalTime.of(22, 30),
                        carlos, bancoDados, es1),
                new Horario(DiaSemana.TERCA, LocalTime.of(19, 0), LocalTime.of(20, 40),
                        mariana, engenhariaSoftware, es1),
                new Horario(DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 40),
                        carlos, redes, cc1),
                new Horario(DiaSemana.TERCA, LocalTime.of(19, 0), LocalTime.of(20, 40),
                        ana, algoritmos, es2),
                new Horario(DiaSemana.QUARTA, LocalTime.of(19, 0), LocalTime.of(20, 40),
                        mariana, bancoDados, es2)));

        log.info("Dados de demonstracao criados: {} professores, {} disciplinas, {} turmas e {} horarios.",
                professorRepository.count(), disciplinaRepository.count(),
                turmaRepository.count(), horarioRepository.count());
    }
}
