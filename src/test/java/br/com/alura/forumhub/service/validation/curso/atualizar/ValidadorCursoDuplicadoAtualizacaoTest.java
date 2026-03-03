package br.com.alura.forumhub.service.validation.curso.atualizar;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.CursoRepository;
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
@DisplayName("Testes unitários de ValidadorCursoDuplicadoAtualizacao")
class ValidadorCursoDuplicadoAtualizacaoTest {

    @InjectMocks
    private ValidadorCursoDuplicadoAtualizacao validador;

    @Mock
    private CursoRepository repository;

    @Test
    @DisplayName("Deve lançar ValidacaoException quando nome já pertence a outro curso")
    void validar_nomeJaExisteEmOutroCurso_deveLancarValidacaoException() {
        var dados = new DadosAtualizacaoCurso("Spring Boot", "Programação");
        given(repository.existsByNomeAndIdNot("Spring Boot", 2L)).willReturn(true);

        assertThatThrownBy(() -> validador.validar(2L, dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Curso já existe");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando nome é único entre outros cursos")
    void validar_nomeUnicoEntreOutrosCursos_naoDeveLancarExcecao() {
        var dados = new DadosAtualizacaoCurso("Spring Framework", "Programação");
        given(repository.existsByNomeAndIdNot("Spring Framework", 1L)).willReturn(false);

        assertThatCode(() -> validador.validar(1L, dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando nome é nulo")
    void validar_nomeNulo_naoDeveLancarExcecao() {
        var dados = new DadosAtualizacaoCurso(null, "Programação");

        assertThatCode(() -> validador.validar(1L, dados)).doesNotThrowAnyException();
    }
}

