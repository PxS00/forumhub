package br.com.alura.forumhub.security;

import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Perfil;
import br.com.alura.forumhub.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes unitários de AutorizacaoService")
class AutorizacaoServiceTest {

    private AutorizacaoService autorizacaoService;

    private Usuario usuarioComum;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        autorizacaoService = new AutorizacaoService();

        Perfil perfilUser = new Perfil(1L, "ROLE_USER");
        Perfil perfilAdmin = new Perfil(2L, "ROLE_ADMIN");

        usuarioComum = new Usuario(1L, "Ana", "ana@email.com", "hash", true, new HashSet<>(Set.of(perfilUser)));
        usuarioAdmin = new Usuario(2L, "Admin", "admin@email.com", "hash", true, new HashSet<>(Set.of(perfilAdmin)));

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // =========================================================
    // usuarioLogado
    // =========================================================

    @Test
    @DisplayName("Deve retornar o usuário logado quando autenticado")
    void usuarioLogado_autenticado_deveRetornarUsuario() {
        autenticarComo(usuarioComum);

        Usuario resultado = autorizacaoService.usuarioLogado();

        assertThat(resultado).isEqualTo(usuarioComum);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando não há autenticação")
    void usuarioLogado_semAutenticacao_deveLancarValidacaoException() {
        assertThatThrownBy(() -> autorizacaoService.usuarioLogado())
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autenticado");
    }

    // =========================================================
    // ehAdmin
    // =========================================================

    @Test
    @DisplayName("Deve retornar true quando usuário tem perfil ROLE_ADMIN")
    void ehAdmin_usuarioAdmin_deveRetornarTrue() {
        assertThat(autorizacaoService.ehAdmin(usuarioAdmin)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando usuário não tem perfil ROLE_ADMIN")
    void ehAdmin_usuarioComum_deveRetornarFalse() {
        assertThat(autorizacaoService.ehAdmin(usuarioComum)).isFalse();
    }

    // =========================================================
    // validarAutorOuAdmin
    // =========================================================

    @Test
    @DisplayName("Não deve lançar exceção quando o usuário logado é o próprio autor")
    void validarAutorOuAdmin_usuarioEhAutor_naoDeveLancarExcecao() {
        autenticarComo(usuarioComum);

        assertThatCode(() -> autorizacaoService.validarAutorOuAdmin(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o usuário logado é admin (mesmo não sendo autor)")
    void validarAutorOuAdmin_usuarioEhAdmin_naoDeveLancarExcecao() {
        autenticarComo(usuarioAdmin);

        // id do autor é 1 (usuarioComum), mas quem está logado é o admin (id=2)
        assertThatCode(() -> autorizacaoService.validarAutorOuAdmin(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário não é o autor nem admin")
    void validarAutorOuAdmin_usuarioNaoEhAutorNemAdmin_deveLancarValidacaoException() {
        autenticarComo(usuarioComum); // id=1, mas tentamos validar idAutor=99

        assertThatThrownBy(() -> autorizacaoService.validarAutorOuAdmin(99L))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");
    }

    // =========================================================
    // validarAdmin
    // =========================================================

    @Test
    @DisplayName("Não deve lançar exceção quando usuário logado é admin")
    void validarAdmin_usuarioAdmin_naoDeveLancarExcecao() {
        autenticarComo(usuarioAdmin);

        assertThatCode(() -> autorizacaoService.validarAdmin()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário logado não é admin")
    void validarAdmin_usuarioComum_deveLancarValidacaoException() {
        autenticarComo(usuarioComum);

        assertThatThrownBy(() -> autorizacaoService.validarAdmin())
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Acesso permitido apenas para administradores");
    }
}

