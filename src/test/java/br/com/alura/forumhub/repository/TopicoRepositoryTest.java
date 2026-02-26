package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Curso;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de repositório de Topico")
class TopicoRepositoryTest {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private TestEntityManager em;

    private Curso cursoSpring;
    private Curso cursoJava;
    private Usuario autor;

    @BeforeEach
    void setUp() {
        cursoSpring = em.persist(new Curso(null, "Spring Boot", "Programação"));
        cursoJava   = em.persist(new Curso(null, "Java Avançado", "Programação"));
        autor       = em.persist(new Usuario(null, "Ana Silva", "ana@email.com", "senha", new java.util.HashSet<>()));
    }

    // Método utilitário para criar e persistir um tópico com data específica
    private Topico criarTopico(String titulo, String mensagem, Curso curso, LocalDateTime dataCriacao) {
        Topico topico = new Topico();
        ReflectionTestUtils.setField(topico, "titulo", titulo);
        ReflectionTestUtils.setField(topico, "mensagem", mensagem);
        ReflectionTestUtils.setField(topico, "dataCriacao", dataCriacao);
        ReflectionTestUtils.setField(topico, "status", StatusTopico.NAO_RESPONDIDO);
        topico.definirAutorECurso(autor, curso);
        return em.persist(topico);
    }

    // =========================================================
    // existsByTituloAndMensagem
    // =========================================================

    @Test
    @DisplayName("existsByTituloAndMensagem - deve retornar true quando tópico com mesmo título e mensagem existe")
    void existsByTituloAndMensagem_topicoDuplicado_deveRetornarTrue() {
        criarTopico("Título A", "Mensagem A", cursoSpring, LocalDateTime.now());
        em.flush();

        boolean existe = topicoRepository.existsByTituloAndMensagem("Título A", "Mensagem A");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByTituloAndMensagem - deve retornar false quando não existe tópico com mesmo título e mensagem")
    void existsByTituloAndMensagem_topicoNaoExiste_deveRetornarFalse() {
        boolean existe = topicoRepository.existsByTituloAndMensagem("Título Inexistente", "Mensagem Inexistente");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByTituloAndMensagem - deve retornar false quando título bate mas mensagem é diferente")
    void existsByTituloAndMensagem_tituloIgualMensagemDiferente_deveRetornarFalse() {
        criarTopico("Título A", "Mensagem A", cursoSpring, LocalDateTime.now());
        em.flush();

        boolean existe = topicoRepository.existsByTituloAndMensagem("Título A", "Mensagem Diferente");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByTituloAndMensagem - deve retornar false quando mensagem bate mas título é diferente")
    void existsByTituloAndMensagem_mensagemIgualTituloDiferente_deveRetornarFalse() {
        criarTopico("Título A", "Mensagem A", cursoSpring, LocalDateTime.now());
        em.flush();

        boolean existe = topicoRepository.existsByTituloAndMensagem("Título Diferente", "Mensagem A");

        assertThat(existe).isFalse();
    }

    // =========================================================
    // buscarPorCursoEAno
    // =========================================================

    @Test
    @DisplayName("buscarPorCursoEAno - deve retornar todos os tópicos quando curso e ano são nulos")
    void buscarPorCursoEAno_semFiltros_deveRetornarTodosOsTopicos() {
        criarTopico("Tópico 1", "Mensagem 1", cursoSpring, LocalDateTime.of(2025, 3, 1, 10, 0));
        criarTopico("Tópico 2", "Mensagem 2", cursoJava,   LocalDateTime.of(2024, 5, 10, 10, 0));
        em.flush();

        var pageable = PageRequest.of(0, 10, Sort.by("dataCriacao").ascending());
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno(null, null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("buscarPorCursoEAno - deve filtrar por nome do curso")
    void buscarPorCursoEAno_comFiltroCurso_deveRetornarApenasTopicosDocurso() {
        criarTopico("Tópico Spring", "Mensagem Spring", cursoSpring, LocalDateTime.of(2025, 1, 1, 10, 0));
        criarTopico("Tópico Java",   "Mensagem Java",   cursoJava,   LocalDateTime.of(2025, 2, 1, 10, 0));
        em.flush();

        var pageable = PageRequest.of(0, 10);
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno("Spring Boot", null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tópico Spring");
        assertThat(resultado.getContent().get(0).getCurso().getNome()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("buscarPorCursoEAno - deve filtrar por ano da data de criação")
    void buscarPorCursoEAno_comFiltroAno_deveRetornarApenasTopicosdoAno() {
        criarTopico("Tópico 2024", "Mensagem 2024", cursoSpring, LocalDateTime.of(2024, 6, 15, 10, 0));
        criarTopico("Tópico 2025", "Mensagem 2025", cursoSpring, LocalDateTime.of(2025, 6, 15, 10, 0));
        em.flush();

        var pageable = PageRequest.of(0, 10);
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno(null, 2025, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tópico 2025");
    }

    @Test
    @DisplayName("buscarPorCursoEAno - deve filtrar por curso e ano combinados")
    void buscarPorCursoEAno_comFiltroCursoEAno_deveRetornarApenasCombinacaoCorreta() {
        criarTopico("Spring 2025",  "Msg",  cursoSpring, LocalDateTime.of(2025, 1, 1, 10, 0));
        criarTopico("Spring 2024",  "Msg2", cursoSpring, LocalDateTime.of(2024, 1, 1, 10, 0));
        criarTopico("Java 2025",    "Msg3", cursoJava,   LocalDateTime.of(2025, 1, 1, 10, 0));
        em.flush();

        var pageable = PageRequest.of(0, 10);
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno("Spring Boot", 2025, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Spring 2025");
    }

    @Test
    @DisplayName("buscarPorCursoEAno - deve retornar página vazia quando curso não existe")
    void buscarPorCursoEAno_cursoInexistente_deveRetornarPaginaVazia() {
        criarTopico("Tópico 1", "Mensagem 1", cursoSpring, LocalDateTime.now());
        em.flush();

        var pageable = PageRequest.of(0, 10);
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno("Curso Inexistente", null, pageable);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarPorCursoEAno - deve respeitar a paginação")
    void buscarPorCursoEAno_semFiltros_deveRespeitarPaginacao() {
        criarTopico("Tópico A", "Mensagem A", cursoSpring, LocalDateTime.of(2025, 1, 1, 10, 0));
        criarTopico("Tópico B", "Mensagem B", cursoSpring, LocalDateTime.of(2025, 1, 2, 10, 0));
        criarTopico("Tópico C", "Mensagem C", cursoSpring, LocalDateTime.of(2025, 1, 3, 10, 0));
        em.flush();

        var pageable = PageRequest.of(0, 2, Sort.by("dataCriacao").ascending());
        Page<Topico> resultado = topicoRepository.buscarPorCursoEAno(null, null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(3);
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tópico A");
    }

    // =========================================================
    // existsByTituloAndMensagemAndIdNot
    // =========================================================

    @Test
    @DisplayName("existsByTituloAndMensagemAndIdNot - deve retornar true quando outro tópico tem mesmo título e mensagem")
    void existsByTituloAndMensagemAndIdNot_outroDuplicado_deveRetornarTrue() {
        Topico t1 = criarTopico("Título Igual", "Mensagem Igual", cursoSpring, LocalDateTime.now());
        Topico t2 = criarTopico("Outro Tópico", "Outra Mensagem", cursoJava,   LocalDateTime.now());
        em.flush();

        // Verifica se, ao atualizar t2 com título/mensagem de t1, haveria duplicidade
        boolean duplicado = topicoRepository.existsByTituloAndMensagemAndIdNot(
                "Título Igual", "Mensagem Igual", t2.getId()
        );

        assertThat(duplicado).isTrue();
    }

    @Test
    @DisplayName("existsByTituloAndMensagemAndIdNot - deve retornar false quando o único tópico com mesmo título/mensagem é o próprio")
    void existsByTituloAndMensagemAndIdNot_mesmoTopico_deveRetornarFalse() {
        Topico t1 = criarTopico("Meu Título", "Minha Mensagem", cursoSpring, LocalDateTime.now());
        em.flush();

        // Editar o próprio tópico com os mesmos dados não deve ser duplicidade
        boolean duplicado = topicoRepository.existsByTituloAndMensagemAndIdNot(
                "Meu Título", "Minha Mensagem", t1.getId()
        );

        assertThat(duplicado).isFalse();
    }

    @Test
    @DisplayName("existsByTituloAndMensagemAndIdNot - deve retornar false quando nenhum tópico possui mesmo título e mensagem")
    void existsByTituloAndMensagemAndIdNot_semDuplicidade_deveRetornarFalse() {
        Topico t1 = criarTopico("Título Único", "Mensagem Única", cursoSpring, LocalDateTime.now());
        em.flush();

        boolean duplicado = topicoRepository.existsByTituloAndMensagemAndIdNot(
                "Outro Título", "Outra Mensagem", t1.getId()
        );

        assertThat(duplicado).isFalse();
    }
}