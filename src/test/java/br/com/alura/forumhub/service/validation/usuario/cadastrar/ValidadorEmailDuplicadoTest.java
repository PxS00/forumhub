package br.com.alura.forumhub.service.validation.usuario.cadastrar;

import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de ValidadorEmailDuplicado")
class ValidadorEmailDuplicadoTest {

    @InjectMocks
    private ValidadorEmailDuplicado validador;

    @Mock
    private UsuarioRepository repository;

    @Test
    @DisplayName("Deve lançar ValidacaoException quando e-mail já está em uso por usuário ativo")
    void validar_emailJaEmUso_deveLancarValidacaoException() {
        var dados = new DadosCadastroUsuario("Ana Silva", "ana@email.com", "Senha@123");
        given(repository.existsByEmailAndAtivoTrue("ana@email.com")).willReturn(true);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando e-mail não está em uso")
    void validar_emailNovo_naoDeveLancarExcecao() {
        var dados = new DadosCadastroUsuario("João Silva", "joao@email.com", "Senha@123");
        given(repository.existsByEmailAndAtivoTrue("joao@email.com")).willReturn(false);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }
}

