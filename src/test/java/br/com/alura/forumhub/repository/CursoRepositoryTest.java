package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Curso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de repositório de Curso")
class CursoRepositoryTest {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private TestEntityManager em;

    private Curso cursoSpring;
    private Curso cursoJava;

    @BeforeEach
    void setUp() {
        cursoSpring = em.persist(new Curso(null, "Spring Boot", "Programação"));
        cursoJava   = em.persist(new Curso(null, "Java Avançado", "Programação"));
        em.flush();
    }

    // =========================================================
    // existsByNome
    // =========================================================

    @Test
    @DisplayName("existsByNome - deve retornar true quando curso com nome existe")
    void existsByNome_nomeExistente_deveRetornarTrue() {
        boolean existe = cursoRepository.existsByNome("Spring Boot");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByNome - deve retornar false quando curso com nome não existe")
    void existsByNome_nomeInexistente_deveRetornarFalse() {
        boolean existe = cursoRepository.existsByNome("Python Básico");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByNome - deve ser case-sensitive")
    void existsByNome_nomeDiferenteMaiusculas_deveRetornarFalse() {
        boolean existe = cursoRepository.existsByNome("spring boot");

        assertThat(existe).isFalse();
    }

    // =========================================================
    // existsByNomeAndIdNot
    // =========================================================

    @Test
    @DisplayName("existsByNomeAndIdNot - deve retornar true quando outro curso possui o mesmo nome")
    void existsByNomeAndIdNot_outroCursoComMesmoNome_deveRetornarTrue() {
        // Ao atualizar cursoJava com o nome de cursoSpring, deve detectar duplicidade
        boolean duplicado = cursoRepository.existsByNomeAndIdNot("Spring Boot", cursoJava.getId());

        assertThat(duplicado).isTrue();
    }

    @Test
    @DisplayName("existsByNomeAndIdNot - deve retornar false quando o único curso com o nome é o próprio")
    void existsByNomeAndIdNot_mesmoCurso_deveRetornarFalse() {
        // Salvar com o mesmo nome do próprio curso não deve ser considerado duplicidade
        boolean duplicado = cursoRepository.existsByNomeAndIdNot("Spring Boot", cursoSpring.getId());

        assertThat(duplicado).isFalse();
    }

    @Test
    @DisplayName("existsByNomeAndIdNot - deve retornar false quando nome não existe em nenhum outro curso")
    void existsByNomeAndIdNot_nomeInexistente_deveRetornarFalse() {
        boolean duplicado = cursoRepository.existsByNomeAndIdNot("Curso Inexistente", cursoSpring.getId());

        assertThat(duplicado).isFalse();
    }
}

