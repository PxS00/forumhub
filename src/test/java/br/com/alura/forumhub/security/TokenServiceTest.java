package br.com.alura.forumhub.security;

import br.com.alura.forumhub.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes unitários de TokenService")
class TokenServiceTest {

    private TokenService tokenService;
    private Usuario usuarioFake;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-1234567890abcdef1234");
        ReflectionTestUtils.setField(tokenService, "expiration", 86400000L);

        usuarioFake = new Usuario(1L, "Ana Silva", "ana@email.com", "hashed", true, new HashSet<>());
    }

    // =========================================================
    // generateToken
    // =========================================================

    @Test
    @DisplayName("Deve gerar token JWT não nulo e não vazio")
    void generateToken_usuarioValido_deveRetornarTokenNaoVazio() {
        String token = tokenService.generateToken(usuarioFake);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Token gerado deve conter subject igual ao username do usuário")
    void generateToken_usuarioValido_subjectDeveSerOEmail() {
        String token = tokenService.generateToken(usuarioFake);
        String subject = tokenService.getSubject(token);

        assertThat(subject).isEqualTo("ana@email.com");
    }

    @Test
    @DisplayName("Tokens gerados para o mesmo usuário são iguais ou no mínimo válidos (idempotente em conteúdo)")
    void generateToken_doisTokensParaMesmoUsuario_ambosValidam() {
        String token1 = tokenService.generateToken(usuarioFake);
        String token2 = tokenService.generateToken(usuarioFake);

        // Ambos devem ser validáveis e retornar o mesmo subject
        assertThat(tokenService.getSubject(token1)).isEqualTo("ana@email.com");
        assertThat(tokenService.getSubject(token2)).isEqualTo("ana@email.com");
    }

    // =========================================================
    // getSubject
    // =========================================================

    @Test
    @DisplayName("Deve retornar o subject correto de um token válido")
    void getSubject_tokenValido_deveRetornarEmailDoUsuario() {
        String token = tokenService.generateToken(usuarioFake);

        String subject = tokenService.getSubject(token);

        assertThat(subject).isEqualTo("ana@email.com");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao validar token inválido (malformado)")
    void getSubject_tokenInvalido_deveLancarRuntimeException() {
        assertThatThrownBy(() -> tokenService.getSubject("token.invalido.qualquer"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired JWT token");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao validar token com assinatura incorreta")
    void getSubject_tokenComAssinaturaErrada_deveLancarRuntimeException() {
        // Gera token com service diferente (outro secret)
        TokenService outroService = new TokenService();
        ReflectionTestUtils.setField(outroService, "secret", "outro-secret-completamente-diferente");
        ReflectionTestUtils.setField(outroService, "expiration", 86400000L);
        String tokenDeOutroServico = outroService.generateToken(usuarioFake);

        assertThatThrownBy(() -> tokenService.getSubject(tokenDeOutroServico))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired JWT token");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao validar token expirado")
    void getSubject_tokenExpirado_deveLancarRuntimeException() {
        // Gera service com expiração negativa (já expirado)
        TokenService expiredService = new TokenService();
        ReflectionTestUtils.setField(expiredService, "secret", "test-secret-key-1234567890abcdef1234");
        ReflectionTestUtils.setField(expiredService, "expiration", -1000L);
        String tokenExpirado = expiredService.generateToken(usuarioFake);

        assertThatThrownBy(() -> tokenService.getSubject(tokenExpirado))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired JWT token");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar validar string vazia")
    void getSubject_tokenVazio_deveLancarRuntimeException() {
        assertThatThrownBy(() -> tokenService.getSubject(""))
                .isInstanceOf(RuntimeException.class);
    }
}


