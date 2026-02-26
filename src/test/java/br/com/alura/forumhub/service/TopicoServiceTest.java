package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.dto.DadosDetalhamentoTopico;
import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.dto.topico.DadosListagemTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.CursoRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.service.validation.topico.atualizar.ValidationAtualizacaoTopico;
import br.com.alura.forumhub.service.validation.topico.cadastrar.ValidationCadastroTopico;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Testes unitários de TopicoService")
class TopicoServiceTest {

    @InjectMocks
    private TopicoService service;

    @Mock
    private TopicoRepository topicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ValidationCadastroTopico validadorCadastro;

    @Mock
    private ValidationAtualizacaoTopico validadorAtualizacao;

    private Usuario autorFake;
    private Curso cursoFake;
    private Topico topicoFake;

    @BeforeEach
    void setUp() {
        // Injeta as listas de validadores nos campos privados via ReflectionTestUtils
        ReflectionTestUtils.setField(service, "validationCadastroTopico", List.of(validadorCadastro));
        ReflectionTestUtils.setField(service, "validationAtualizacaoTopico", List.of(validadorAtualizacao));

        autorFake = new Usuario(1L, "Ana Silva", "ana@email.com", "senha123", new java.util.HashSet<>());
        cursoFake = new Curso(1L, "Spring Boot", "Programação");

        topicoFake = new Topico(
                new DadosCadastroTopico("Dúvida sobre Spring", "Como funciona o IoC?", 1L, 1L)
        );
        topicoFake.definirAutorECurso(autorFake, cursoFake);
        ReflectionTestUtils.setField(topicoFake, "id", 1L);
        ReflectionTestUtils.setField(topicoFake, "dataCriacao", LocalDateTime.of(2025, 1, 10, 10, 0));
    }

    // =========================================================
    // CADASTRAR
    // =========================================================

    @Test
    @DisplayName("Deve cadastrar tópico com sucesso quando dados válidos")
    void cadastrar_comDadosValidos_deveRetornarDetalhamento() {
        var dados = new DadosCadastroTopico("Dúvida sobre Spring", "Como funciona o IoC?", 1L, 1L);

        willDoNothing().given(validadorCadastro).validar(dados);
        given(usuarioRepository.getReferenceById(1L)).willReturn(autorFake);
        given(cursoRepository.getReferenceById(1L)).willReturn(cursoFake);
        given(topicoRepository.save(any(Topico.class))).willAnswer(invocation -> {
            Topico t = invocation.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L);
            return t;
        });

        DadosDetalhamentoTopico resultado = service.cadastrar(dados);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Dúvida sobre Spring");
        assertThat(resultado.mensagem()).isEqualTo("Como funciona o IoC?");
        assertThat(resultado.nomeAutor()).isEqualTo("Ana Silva");
        assertThat(resultado.nomeCurso()).isEqualTo("Spring Boot");
        assertThat(resultado.id()).isEqualTo(1L);

        then(topicoRepository).should().save(any(Topico.class));
    }

    @Test
    @DisplayName("Deve chamar todos os validadores de cadastro")
    void cadastrar_deveInvocarTodosOsValidadores() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 1L, 1L);

        given(usuarioRepository.getReferenceById(1L)).willReturn(autorFake);
        given(cursoRepository.getReferenceById(1L)).willReturn(cursoFake);
        given(topicoRepository.save(any())).willAnswer(i -> {
            Topico t = i.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 2L);
            return t;
        });

        service.cadastrar(dados);

        then(validadorCadastro).should().validar(dados);
    }

    @Test
    @DisplayName("Deve lançar TopicoDuplicadoException quando validador detectar duplicidade")
    void cadastrar_topicoDuplicado_deveLancarTopicoDuplicadoException() {
        var dados = new DadosCadastroTopico("Título duplicado", "Mesma mensagem", 1L, 1L);

        willThrow(new TopicoDuplicadoException()).given(validadorCadastro).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(TopicoDuplicadoException.class)
                .hasMessageContaining("Tópico já cadastrado");

        then(topicoRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando autor não existe")
    void cadastrar_autorInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 1L, 99L);

        willThrow(new EntityNotFoundException("Autor não encontrado")).given(validadorCadastro).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Autor não encontrado");

        then(topicoRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando curso não existe")
    void cadastrar_cursoInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosCadastroTopico("Título", "Mensagem", 99L, 1L);

        willThrow(new EntityNotFoundException("Curso não encontrado")).given(validadorCadastro).validar(dados);

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");

        then(topicoRepository).should(never()).save(any());
    }

    // =========================================================
    // LISTAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar página de tópicos sem filtro")
    void listar_semFiltros_deveRetornarPaginaDeTopicos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topico> pagina = new PageImpl<>(List.of(topicoFake));
        given(topicoRepository.buscarPorCursoEAno(null, null, pageable)).willReturn(pagina);

        Page<DadosListagemTopico> resultado = service.listar(null, null, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Dúvida sobre Spring");
        assertThat(resultado.getContent().get(0).nomeAutor()).isEqualTo("Ana Silva");
        assertThat(resultado.getContent().get(0).nomeCurso()).isEqualTo("Spring Boot");
        assertThat(resultado.getContent().get(0).status()).isEqualTo(StatusTopico.NAO_RESPONDIDO);
    }

    @Test
    @DisplayName("Deve retornar página filtrada por curso")
    void listar_comFiltroDeCurso_deveRetornarApenasTopiscosDoCurso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topico> pagina = new PageImpl<>(List.of(topicoFake));
        given(topicoRepository.buscarPorCursoEAno("Spring Boot", null, pageable)).willReturn(pagina);

        Page<DadosListagemTopico> resultado = service.listar("Spring Boot", null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nomeCurso()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("Deve retornar página filtrada por ano")
    void listar_comFiltroDeAno_deveRetornarApenasTopicosDoAno() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topico> pagina = new PageImpl<>(List.of(topicoFake));
        given(topicoRepository.buscarPorCursoEAno(null, 2025, pageable)).willReturn(pagina);

        Page<DadosListagemTopico> resultado = service.listar(null, 2025, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).dataCriacao().getYear()).isEqualTo(2025);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando nenhum tópico encontrado")
    void listar_semTopicos_deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        given(topicoRepository.buscarPorCursoEAno(any(), any(), eq(pageable)))
                .willReturn(Page.empty(pageable));

        Page<DadosListagemTopico> resultado = service.listar("CursoInexistente", 2000, pageable);

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    // DETALHAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar detalhamento do tópico quando ID válido")
    void detalhar_idValido_deveRetornarDetalhamento() {
        given(topicoRepository.findById(1L)).willReturn(Optional.of(topicoFake));

        DadosDetalhamentoTopico resultado = service.detalhar(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.titulo()).isEqualTo("Dúvida sobre Spring");
        assertThat(resultado.mensagem()).isEqualTo("Como funciona o IoC?");
        assertThat(resultado.nomeAutor()).isEqualTo("Ana Silva");
        assertThat(resultado.nomeCurso()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando tópico não encontrado pelo ID")
    void detalhar_idInexistente_deveLancarEntityNotFoundException() {
        given(topicoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Tópico não encontrado");
    }

    // =========================================================
    // ATUALIZAR
    // =========================================================

    @Test
    @DisplayName("Deve atualizar tópico com sucesso quando dados válidos")
    void atualizar_comDadosValidos_deveRetornarTopicaAtualizado() {
        var dados = new DadosAtualizacaoTopico("Novo título", "Nova mensagem", 1L);
        Curso cursoBd = new Curso(1L, "Spring Boot", "Programação");

        given(topicoRepository.findById(1L)).willReturn(Optional.of(topicoFake));
        given(cursoRepository.findById(1L)).willReturn(Optional.of(cursoBd));
        willDoNothing().given(validadorAtualizacao).validar(1L, dados);

        DadosDetalhamentoTopico resultado = service.atualizar(1L, dados);

        assertThat(resultado.titulo()).isEqualTo("Novo título");
        assertThat(resultado.mensagem()).isEqualTo("Nova mensagem");
        assertThat(resultado.nomeCurso()).isEqualTo("Spring Boot");
        then(validadorAtualizacao).should().validar(1L, dados);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar tópico inexistente")
    void atualizar_idInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosAtualizacaoTopico("Título", "Mensagem", 1L);
        given(topicoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(999L, dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Tópico não encontrado");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar com curso inexistente")
    void atualizar_cursoInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosAtualizacaoTopico("Título", "Mensagem", 99L);

        given(topicoRepository.findById(1L)).willReturn(Optional.of(topicoFake));
        given(cursoRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(1L, dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Curso não encontrado");
    }

    @Test
    @DisplayName("Deve lançar TopicoDuplicadoException ao atualizar com título e mensagem já existentes")
    void atualizar_topicoDuplicado_deveLancarTopicoDuplicadoException() {
        var dados = new DadosAtualizacaoTopico("Título duplicado", "Mensagem duplicada", 1L);

        given(topicoRepository.findById(1L)).willReturn(Optional.of(topicoFake));
        given(cursoRepository.findById(1L)).willReturn(Optional.of(cursoFake));
        willThrow(new TopicoDuplicadoException()).given(validadorAtualizacao).validar(1L, dados);

        assertThatThrownBy(() -> service.atualizar(1L, dados))
                .isInstanceOf(TopicoDuplicadoException.class)
                .hasMessageContaining("Tópico já cadastrado");
    }

    // =========================================================
    // DELETAR
    // =========================================================

    @Test
    @DisplayName("Deve deletar tópico com sucesso quando ID válido")
    void deletar_idValido_deveDeletarSemErros() {
        given(topicoRepository.findById(1L)).willReturn(Optional.of(topicoFake));
        willDoNothing().given(topicoRepository).deleteById(1L);

        assertThatCode(() -> service.deletar(1L)).doesNotThrowAnyException();

        then(topicoRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar tópico inexistente")
    void deletar_idInexistente_deveLancarEntityNotFoundException() {
        given(topicoRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Tópico não encontrado");

        then(topicoRepository).should(never()).deleteById(any());
    }
}

