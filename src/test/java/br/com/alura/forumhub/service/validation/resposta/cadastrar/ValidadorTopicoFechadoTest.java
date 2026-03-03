package br.com.alura.forumhub.service.validation.resposta.cadastrar;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de ValidadorTopicoFechado")
class ValidadorTopicoFechadoTest {

    @InjectMocks
    private ValidadorTopicoFechado validador;

    @Mock
    private TopicoRepository repository;

    private Topico topicoAberto;
    private Topico topicoFechado;

    @BeforeEach
    void setUp() {
        Usuario autor = new Usuario(1L, "Ana", "ana@email.com", "hash", true, new HashSet<>());
        Curso curso = new Curso(1L, "Spring Boot", "Programação");

        topicoAberto = new Topico(new DadosCadastroTopico("Título", "Mensagem", 1L, 1L));
        topicoAberto.definirAutorECurso(autor, curso);
        ReflectionTestUtils.setField(topicoAberto, "id", 1L);

        topicoFechado = new Topico(new DadosCadastroTopico("Título", "Mensagem", 1L, 1L));
        topicoFechado.definirAutorECurso(autor, curso);
        ReflectionTestUtils.setField(topicoFechado, "id", 2L);
        ReflectionTestUtils.setField(topicoFechado, "status", StatusTopico.FECHADO);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando tópico está fechado")
    void validar_topicoFechado_deveLancarValidacaoException() {
        var dados = new DadosCadastroResposta("Mensagem", 2L, 1L);
        given(repository.findById(2L)).willReturn(Optional.of(topicoFechado));

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Não é permitido responder um tópico fechado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando tópico está aberto")
    void validar_topicoAberto_naoDeveLancarExcecao() {
        var dados = new DadosCadastroResposta("Mensagem", 1L, 1L);
        given(repository.findById(1L)).willReturn(Optional.of(topicoAberto));

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando tópico não encontrado")
    void validar_topicoNaoEncontrado_deveLancarValidacaoException() {
        var dados = new DadosCadastroResposta("Mensagem", 99L, 1L);
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Tópico não encontrado");
    }
}

