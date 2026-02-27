package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
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
@DisplayName("Testes unitários de ValidarTopicoDuplicado")
class ValidarTopicoDuplicadoTest {

    @InjectMocks
    private ValidarTopicoDuplicado validador;

    @Mock
    private TopicoRepository repository;

    @Test
    @DisplayName("Deve lançar TopicoDuplicadoException quando título e mensagem já existem")
    void validar_topicoDuplicado_deveLancarTopicoDuplicadoException() {
        var dados = new DadosCadastroTopico("Título existente", "Mensagem existente", 1L, 1L);
        given(repository.existsByTituloAndMensagem("Título existente", "Mensagem existente")).willReturn(true);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(TopicoDuplicadoException.class)
                .hasMessageContaining("Tópico já cadastrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando título e mensagem são únicos")
    void validar_topicoNovoCombinacao_naoDeveLancarExcecao() {
        var dados = new DadosCadastroTopico("Título novo", "Mensagem nova", 1L, 1L);
        given(repository.existsByTituloAndMensagem("Título novo", "Mensagem nova")).willReturn(false);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando apenas o título é igual")
    void validar_apenasTituloIgual_naoDeveLancarExcecao() {

        var dados = new DadosCadastroTopico(
                "Título existente",
                "Mensagem diferente",
                1L,
                1L
        );

        given(repository.existsByTituloAndMensagem(
                "Título existente",
                "Mensagem diferente"
        )).willReturn(false);

        assertThatCode(() -> validador.validar(dados))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando apenas a mensagem é igual")
    void validar_apenasMensagemIgual_naoDeveLancarExcecao() {

        var dados = new DadosCadastroTopico(
                "Título diferente",
                "Mensagem existente",
                1L,
                1L
        );

        given(repository.existsByTituloAndMensagem(
                "Título diferente",
                "Mensagem existente"
        )).willReturn(false);

        assertThatCode(() -> validador.validar(dados))
                .doesNotThrowAnyException();
    }
}

