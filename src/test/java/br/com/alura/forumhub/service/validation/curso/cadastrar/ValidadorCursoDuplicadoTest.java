package br.com.alura.forumhub.service.validation.curso.cadastrar;

import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
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
@DisplayName("Testes unitários de ValidadorCursoDuplicado")
class ValidadorCursoDuplicadoTest {

    @InjectMocks
    private ValidadorCursoDuplicado validador;

    @Mock
    private CursoRepository repository;

    @Test
    @DisplayName("Deve lançar ValidacaoException quando nome de curso já existe")
    void validar_nomeDuplicado_deveLancarValidacaoException() {
        var dados = new DadosCadastroCurso("Spring Boot", "Programação");
        given(repository.existsByNome("Spring Boot")).willReturn(true);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Curso já existe");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando nome de curso é único")
    void validar_nomeUnico_naoDeveLancarExcecao() {
        var dados = new DadosCadastroCurso("Novo Curso", "Categoria");
        given(repository.existsByNome("Novo Curso")).willReturn(false);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }
}

