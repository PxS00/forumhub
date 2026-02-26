package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
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
@DisplayName("Testes unitários de ValidarDuplicidadeTopicoAoAtualizar")
class ValidarDuplicidadeTopicoAoAtualizarTest {

    @InjectMocks
    private ValidarDuplicidadeTopicoAoAtualizar validador;

    @Mock
    private TopicoRepository repository;

    @Test
    @DisplayName("Deve lançar TopicoDuplicadoException quando outro tópico já tem mesmo título e mensagem")
    void validar_outrotopicoComMesmosDados_deveLancarTopicoDuplicadoException() {
        var dados = new DadosAtualizacaoTopico("Título duplicado", "Mensagem duplicada", 1L);
        given(repository.existsByTituloAndMensagemAndIdNot("Título duplicado", "Mensagem duplicada", 1L))
                .willReturn(true);

        assertThatThrownBy(() -> validador.validar(1L, dados))
                .isInstanceOf(TopicoDuplicadoException.class)
                .hasMessageContaining("Tópico já cadastrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando nenhum outro tópico possui o mesmo título e mensagem")
    void validar_combinacaoUnica_naoDeveLancarExcecao() {
        var dados = new DadosAtualizacaoTopico("Título único", "Mensagem única", 1L);
        given(repository.existsByTituloAndMensagemAndIdNot("Título único", "Mensagem única", 1L))
                .willReturn(false);

        assertThatCode(() -> validador.validar(1L, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o próprio tópico mantém o mesmo título e mensagem (edição sem mudança)")
    void validar_mesmotopicoSemMudanca_naoDeveLancarExcecao() {
        var dados = new DadosAtualizacaoTopico("Meu título", "Minha mensagem", 1L);
        // o id do próprio tópico é excluído da busca — portanto retorna false
        given(repository.existsByTituloAndMensagemAndIdNot("Meu título", "Minha mensagem", 5L))
                .willReturn(false);

        assertThatCode(() -> validador.validar(5L, dados)).doesNotThrowAnyException();
    }
}

