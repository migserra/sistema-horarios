package br.com.unisales.horarios.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de excecoes da API. Cada tipo de excecao e
 * convertido em uma resposta JSON padronizada com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(ResourceNotFoundException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    public ResponseEntity<ErroResposta> tratarConflitoHorario(ConflitoHorarioException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<ErroResposta> tratarHorarioInvalido(HorarioInvalidoException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(erro -> campos.putIfAbsent("corpo", erro.getDefaultMessage()));

        ErroResposta resposta = ErroResposta.deValidacao(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos estao invalidos.",
                request.getRequestURI(),
                campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisicao ausente ou mal formatado. Verifique os formatos de data/hora (HH:mm) e os valores de diaSemana.",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarParametroInvalido(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Parametro invalido: " + ex.getName() + ". Verifique o valor informado na URL.",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "Operacao viola uma restricao de integridade do banco de dados (registro duplicado ou referencia em uso).",
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(NoResourceFoundException ex,
                                                                   HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Endpoint nao encontrado.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar a requisicao", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado ao processar a requisicao.",
                request);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String mensagem,
                                                   HttpServletRequest request) {
        ErroResposta resposta = ErroResposta.de(status, mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(resposta);
    }
}
