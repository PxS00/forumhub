package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
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
@DisplayName("Testes unitários de ValidarAutorExiste")
class ValidarAutorExisteTest {

    @InjectMocks
    private ValidarAutorExiste validador;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando autor não existe no banco")
    void validar_autorInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 1L, 99L);
        given(usuarioRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Autor não encontrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando autor existe no banco")
    void validar_autorExistente_naoDeveLancarExcecao() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 1L, 1L);
        given(usuarioRepository.existsById(1L)).willReturn(true);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }
}

