package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de ValidarDuplicidadeTopicoAoAtualizar")
class ValidadorDuplicidadeTopicoAoAtualizarTest {

    @InjectMocks
    private ValidadorDuplicidadeTopicoAoAtualizar validador;

    @Mock
    private TopicoRepository repository;

    @Test
    @DisplayName("Deve lançar TopicoDuplicadoException quando outro tópico já tem mesmo título e mensagem")
    void validar_outrotopicoComMesmosDados_deveLancarTopicoDuplicadoException() {
        var topico = criarTopico(1L);
        var dados = new DadosAtualizacaoTopico("Título duplicado", "Mensagem duplicada", 1L);
        given(repository.existsByTituloAndMensagemAndIdNot("Título duplicado", "Mensagem duplicada", 1L))
                .willReturn(true);

        assertThatThrownBy(() -> validador.validar(topico, dados))
                .isInstanceOf(TopicoDuplicadoException.class)
                .hasMessageContaining("Tópico já cadastrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando nenhum outro tópico possui o mesmo título e mensagem")
    void validar_combinacaoUnica_naoDeveLancarExcecao() {
        var topico = criarTopico(1L);
        var dados = new DadosAtualizacaoTopico("Título único", "Mensagem única", 1L);
        given(repository.existsByTituloAndMensagemAndIdNot("Título único", "Mensagem única", 1L))
                .willReturn(false);

        assertThatCode(() -> validador.validar(topico, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o próprio tópico mantém o mesmo título e mensagem (edição sem mudança)")
    void validar_mesmotopicoSemMudanca_naoDeveLancarExcecao() {
        var topico = criarTopico(5L);
        var dados = new DadosAtualizacaoTopico("Meu título", "Minha mensagem", 1L);
        given(repository.existsByTituloAndMensagemAndIdNot("Meu título", "Minha mensagem", 5L))
                .willReturn(false);

        assertThatCode(() -> validador.validar(topico, dados)).doesNotThrowAnyException();
    }

    private Topico criarTopico(Long id) {
        Topico topico = new Topico(new DadosCadastroTopico("Título", "Mensagem", 1L, 1L));
        ReflectionTestUtils.setField(topico, "id", id);
        return topico;
    }
}
