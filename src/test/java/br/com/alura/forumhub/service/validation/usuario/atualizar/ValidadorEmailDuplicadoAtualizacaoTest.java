package br.com.alura.forumhub.service.validation.usuario.atualizar;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de ValidadorEmailDuplicadoAtualizacao")
class ValidadorEmailDuplicadoAtualizacaoTest {

    @InjectMocks
    private ValidadorEmailDuplicadoAtualizacao validador;

    @Mock
    private UsuarioRepository repository;

    private Usuario criarUsuario(String email) {
        return new Usuario(1L, "Nome", email, "hash", true, new HashSet<>());
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando novo e-mail já pertence a usuário ativo diferente")
    void validar_emailJaUsadoPorOutroUsuarioAtivo_deveLancarValidacaoException() {
        Usuario usuario = criarUsuario("ana@email.com");
        var dados = new DadosAtualizacaoUsuario("Ana", "outro@email.com", "Senha@123");

        given(repository.existsByEmailAndAtivoTrue("outro@email.com")).willReturn(true);

        assertThatThrownBy(() -> validador.validar(usuario, dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Já existe um usuário ativo com este e-mail");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando e-mail não mudou")
    void validar_mesmoEmail_naoDeveLancarExcecao() {
        Usuario usuario = criarUsuario("ana@email.com");
        var dados = new DadosAtualizacaoUsuario("Ana", "ana@email.com", "Senha@123");

        assertThatCode(() -> validador.validar(usuario, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando novo e-mail está disponível")
    void validar_emailDisponivel_naoDeveLancarExcecao() {
        Usuario usuario = criarUsuario("ana@email.com");
        var dados = new DadosAtualizacaoUsuario("Ana", "novo@email.com", "Senha@123");

        given(repository.existsByEmailAndAtivoTrue("novo@email.com")).willReturn(false);

        assertThatCode(() -> validador.validar(usuario, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando e-mail é nulo")
    void validar_emailNulo_naoDeveLancarExcecao() {
        Usuario usuario = criarUsuario("ana@email.com");
        var dados = new DadosAtualizacaoUsuario("Ana", null, "Senha@123");

        assertThatCode(() -> validador.validar(usuario, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando novo e-mail pertence apenas a usuário inativo")
    void validar_emailPertenceAUsuarioInativo_naoDeveLancarExcecao() {
        // existsByEmailAndAtivoTrue retorna false pois o e-mail pertence a um usuário inativo
        Usuario usuario = criarUsuario("ana@email.com");
        var dados = new DadosAtualizacaoUsuario("Ana", "inativo@email.com", "Senha@123");

        given(repository.existsByEmailAndAtivoTrue("inativo@email.com")).willReturn(false);

        assertThatCode(() -> validador.validar(usuario, dados)).doesNotThrowAnyException();
    }
}



