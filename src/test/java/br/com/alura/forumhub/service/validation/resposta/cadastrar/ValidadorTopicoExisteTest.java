package br.com.alura.forumhub.service.validation.resposta.cadastrar;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.TopicoRepository;
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
@DisplayName("Testes unitários de ValidadorTopicoExiste")
class ValidadorTopicoExisteTest {

    @InjectMocks
    private ValidadorTopicoExiste validador;

    @Mock
    private TopicoRepository repository;

    @Test
    @DisplayName("Deve lançar ValidacaoException quando tópico não existe")
    void validar_topicoNaoExiste_deveLancarValidacaoException() {
        var dados = new DadosCadastroResposta("Mensagem", 99L, 1L);
        given(repository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Tópico não encontrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando tópico existe")
    void validar_topicoExiste_naoDeveLancarExcecao() {
        var dados = new DadosCadastroResposta("Mensagem", 1L, 1L);
        given(repository.existsById(1L)).willReturn(true);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }
}

