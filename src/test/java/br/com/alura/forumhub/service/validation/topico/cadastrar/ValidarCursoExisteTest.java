package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.repository.CursoRepository;
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
@DisplayName("Testes unitários de ValidarCursoExiste")
class ValidarCursoExisteTest {

    @InjectMocks
    private ValidarCursoExiste validador;

    @Mock
    private CursoRepository cursoRepository;

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando curso não existe no banco")
    void validar_cursoInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 99L, 1L);
        given(cursoRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> validador.validar(dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando curso existe no banco")
    void validar_cursoExistente_naoDeveLancarExcecao() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 1L, 1L);
        given(cursoRepository.existsById(1L)).willReturn(true);

        assertThatCode(() -> validador.validar(dados)).doesNotThrowAnyException();
    }
}

