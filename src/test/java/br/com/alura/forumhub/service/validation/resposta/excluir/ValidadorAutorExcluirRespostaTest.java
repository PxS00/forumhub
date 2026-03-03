package br.com.alura.forumhub.service.validation.resposta.excluir;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de ValidadorAutorExcluirResposta")
class ValidadorAutorExcluirRespostaTest {

    @InjectMocks
    private ValidadorAutorExcluirResposta validador;

    @Mock
    private AutorizacaoService autorizacaoService;

    private Resposta respostaFake;

    @BeforeEach
    void setUp() {
        Usuario autor = new Usuario(1L, "Ana", "ana@email.com", "hash", true, new HashSet<>());
        Curso curso = new Curso(1L, "Spring Boot", "Programação");
        Topico topico = new Topico(new DadosCadastroTopico("Título", "Mensagem", 1L, 1L));
        topico.definirAutorECurso(autor, curso);
        ReflectionTestUtils.setField(topico, "id", 1L);

        respostaFake = new Resposta(new DadosCadastroResposta("Resposta", 1L, 1L));
        respostaFake.definirTopicoEAutor(topico, autor);
        ReflectionTestUtils.setField(respostaFake, "id", 1L);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o usuário é o autor da resposta")
    void validar_usuarioEhAutor_naoDeveLancarExcecao() {
        willDoNothing().given(autorizacaoService).validarAutorOuAdmin(1L);

        assertThatCode(() -> validador.validar(respostaFake)).doesNotThrowAnyException();

        then(autorizacaoService).should().validarAutorOuAdmin(1L);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando o usuário não é o autor")
    void validar_usuarioNaoEhAutor_deveLancarValidacaoException() {
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(autorizacaoService).validarAutorOuAdmin(1L);

        assertThatThrownBy(() -> validador.validar(respostaFake))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");
    }
}

