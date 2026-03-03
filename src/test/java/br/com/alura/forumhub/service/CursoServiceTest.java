package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;
import br.com.alura.forumhub.dto.curso.DadosCadastroCurso;
import br.com.alura.forumhub.dto.curso.DadosDetalhamentoCurso;
import br.com.alura.forumhub.dto.curso.DadosListagemCurso;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.service.validation.curso.atualizar.ValidationAtualizacaoCurso;
import br.com.alura.forumhub.service.validation.curso.cadastrar.ValidationCadastroCurso;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Testes unitários de CursoService")
class CursoServiceTest {

    @InjectMocks
    private CursoService service;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ValidationCadastroCurso validadorCadastro;

    @Mock
    private ValidationAtualizacaoCurso validadorAtualizacao;

    private Curso cursoFake;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "validationCadastroCursos", List.of(validadorCadastro));
        ReflectionTestUtils.setField(service, "validationAtualizacaoCursos", List.of(validadorAtualizacao));

        cursoFake = new Curso(1L, "Spring Boot", "Programação");
    }

    // =========================================================
    // CADASTRAR
    // =========================================================

    @Test
    @DisplayName("Deve cadastrar curso com sucesso quando dados válidos")
    void cadastrar_comDadosValidos_deveRetornarDetalhamento() {
        var dados = new DadosCadastroCurso("Spring Boot", "Programação");

        willDoNothing().given(validadorCadastro).validar(dados);
        given(cursoRepository.save(any(Curso.class))).willAnswer(invocation -> {
            Curso c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        DadosDetalhamentoCurso resultado = service.cadastrar(dados);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("Spring Boot");
        assertThat(resultado.categoria()).isEqualTo("Programação");
        assertThat(resultado.id()).isEqualTo(1L);

        then(cursoRepository).should().save(any(Curso.class));
    }

    @Test
    @DisplayName("Deve chamar todos os validadores de cadastro")
    void cadastrar_deveInvocarTodosOsValidadores() {
        var dados = new DadosCadastroCurso("Java", "Programação");

        given(cursoRepository.save(any())).willAnswer(i -> {
            Curso c = i.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 2L);
            return c;
        });

        service.cadastrar(dados);

        then(validadorCadastro).should().validar(dados);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando curso duplicado")
    void cadastrar_cursoDuplicado_deveLancarValidacaoException() {
        var dados = new DadosCadastroCurso("Spring Boot", "Programação");

        willThrow(new ValidacaoException("Curso já existe")).given(validadorCadastro).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Curso já existe");

        then(cursoRepository).should(never()).save(any());
    }

    // =========================================================
    // LISTAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar página de cursos")
    void listar_deveRetornarPaginaDeCursos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curso> pagina = new PageImpl<>(List.of(cursoFake));
        given(cursoRepository.findAll(pageable)).willReturn(pagina);

        Page<DadosListagemCurso> resultado = service.listar(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Spring Boot");
        assertThat(resultado.getContent().get(0).categoria()).isEqualTo("Programação");
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há cursos")
    void listar_semCursos_deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        given(cursoRepository.findAll(pageable)).willReturn(Page.empty(pageable));

        Page<DadosListagemCurso> resultado = service.listar(pageable);

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    // DETALHAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar detalhamento do curso quando ID válido")
    void detalhar_idValido_deveRetornarDetalhamento() {
        given(cursoRepository.findById(1L)).willReturn(Optional.of(cursoFake));

        DadosDetalhamentoCurso resultado = service.detalhar(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Spring Boot");
        assertThat(resultado.categoria()).isEqualTo("Programação");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando curso não encontrado pelo ID")
    void detalhar_idInexistente_deveLancarEntityNotFoundException() {
        given(cursoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");
    }

    // =========================================================
    // ATUALIZAR
    // =========================================================

    @Test
    @DisplayName("Deve atualizar curso com sucesso quando dados válidos")
    void atualizar_comDadosValidos_deveRetornarCursoAtualizado() {
        var dados = new DadosAtualizacaoCurso("Spring Framework", "Back-end");

        given(cursoRepository.findById(1L)).willReturn(Optional.of(cursoFake));
        willDoNothing().given(validadorAtualizacao).validar(1L, dados);

        DadosDetalhamentoCurso resultado = service.atualizar(1L, dados);

        assertThat(resultado.nome()).isEqualTo("Spring Framework");
        assertThat(resultado.categoria()).isEqualTo("Back-end");
        then(validadorAtualizacao).should().validar(1L, dados);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar curso inexistente")
    void atualizar_idInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosAtualizacaoCurso("Nome", "Categoria");
        given(cursoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(999L, dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando nome já existe ao atualizar")
    void atualizar_nomeJaExiste_deveLancarValidacaoException() {
        var dados = new DadosAtualizacaoCurso("Spring Boot", "Programação");

        willThrow(new ValidacaoException("Curso já existe")).given(validadorAtualizacao).validar(1L, dados);

        assertThatThrownBy(() -> service.atualizar(1L, dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Curso já existe");
    }

    // =========================================================
    // DELETAR
    // =========================================================

    @Test
    @DisplayName("Deve deletar curso com sucesso quando ID válido")
    void deletar_idValido_deveDeletarSemErros() {
        given(cursoRepository.findById(1L)).willReturn(Optional.of(cursoFake));
        willDoNothing().given(cursoRepository).delete(cursoFake);

        assertThatCode(() -> service.deletar(1L)).doesNotThrowAnyException();

        then(cursoRepository).should().delete(cursoFake);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar curso inexistente")
    void deletar_idInexistente_deveLancarEntityNotFoundException() {
        given(cursoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");

        then(cursoRepository).should(never()).delete(any());
    }
}

