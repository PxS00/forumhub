package br.com.alura.forumhub.exception.handler;

import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.exception.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 — Entidade não encontrada
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle404(EntityNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        404,
                        "Not Found",
                        ex.getMessage()
                )
        );
    }

    // 400 — Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationError>> handle400Validation(MethodArgumentNotValidException ex) {

        var errors = ex.getFieldErrors()
                .stream()
                .map(ValidationError::new)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    // 400 — JSON inválido / corpo malformado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle400Json(HttpMessageNotReadableException ex) {

        log.warn("Erro ao ler corpo da requisição", ex);
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        400,
                        "Requisição inválida",
                        "Corpo da requisição mal formatado"
                )
        );
    }

    // 401 — Credenciais inválidas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handle401BadCredentials() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(401, "Unauthorized", "Credenciais inválidas")
        );
    }

    // 401 — Falha na autenticação
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handle401Authentication() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(401, "Unauthorized", "Falha na autenticação")
        );
    }

    // 403 — Acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handle403() {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(403, "Forbidden", "Acesso negado")
        );
    }

    // 409 — Regras de negócio (duplicidade)
    @ExceptionHandler(TopicoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handle409(TopicoDuplicadoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(409, "Conflict", ex.getMessage())
        );
    }

    // 400 — Regras de negócio genéricas
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(ValidacaoException ex) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "Bad Request", ex.getMessage())
        );
    }

    // 500 — Erro interno (seguro)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle500(Exception ex) {

        log.error("Erro interno", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        500,
                        "Internal Server Error",
                        "Erro interno do servidor"
                )
        );
    }


    // DTOs de erro

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
        public ErrorResponse(int status, String error, String message) {
            this(status, error, message, LocalDateTime.now());
        }
    }

    public record ValidationError(
            String field,
            String message
    ) {
        public ValidationError(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }
}