package br.com.alura.forumhub.repository;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de repositório de Usuario")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager em;

    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = em.persist(new Usuario(
                null, "João Ativo", "joao@email.com", "hash123", true, new HashSet<>()
        ));
        usuarioInativo = em.persist(new Usuario(
                null, "Maria Inativa", "maria@email.com", "hash456", false, new HashSet<>()
        ));
        em.flush();
    }

    // =========================================================
    // findByEmail
    // =========================================================

    @Test
    @DisplayName("findByEmail - deve retornar o usuário quando e-mail existe")
    void findByEmail_emailExistente_deveRetornarUsuario() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("joao@email.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("João Ativo");
    }

    @Test
    @DisplayName("findByEmail - deve retornar vazio quando e-mail não existe")
    void findByEmail_emailInexistente_deveRetornarVazio() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("naoexiste@email.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByEmail - deve retornar usuário inativo também")
    void findByEmail_usuarioInativo_deveRetornarMesmoInativo() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("maria@email.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getAtivo()).isFalse();
    }

    // =========================================================
    // existsByEmail
    // =========================================================

    @Test
    @DisplayName("existsByEmail - deve retornar true quando e-mail existe")
    void existsByEmail_emailExistente_deveRetornarTrue() {
        boolean existe = usuarioRepository.existsByEmail("joao@email.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - deve retornar false quando e-mail não existe")
    void existsByEmail_emailInexistente_deveRetornarFalse() {
        boolean existe = usuarioRepository.existsByEmail("fantasma@email.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByEmail - deve retornar true mesmo para usuário inativo")
    void existsByEmail_usuarioInativo_deveRetornarTrue() {
        boolean existe = usuarioRepository.existsByEmail("maria@email.com");

        assertThat(existe).isTrue();
    }

    // =========================================================
    // existsByEmailAndAtivoTrue
    // =========================================================

    @Test
    @DisplayName("existsByEmailAndAtivoTrue - deve retornar true para usuário ativo com e-mail existente")
    void existsByEmailAndAtivoTrue_ativoComEmail_deveRetornarTrue() {
        boolean existe = usuarioRepository.existsByEmailAndAtivoTrue("joao@email.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmailAndAtivoTrue - deve retornar false para usuário inativo")
    void existsByEmailAndAtivoTrue_usuarioInativo_deveRetornarFalse() {
        boolean existe = usuarioRepository.existsByEmailAndAtivoTrue("maria@email.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndAtivoTrue - deve retornar false quando e-mail não existe")
    void existsByEmailAndAtivoTrue_emailInexistente_deveRetornarFalse() {
        boolean existe = usuarioRepository.existsByEmailAndAtivoTrue("naoexiste@email.com");

        assertThat(existe).isFalse();
    }

    // =========================================================
    // findByAtivoTrue
    // =========================================================

    @Test
    @DisplayName("findByAtivoTrue - deve retornar apenas usuários ativos")
    void findByAtivoTrue_deveRetornarApenasAtivos() {
        Page<Usuario> resultado = usuarioRepository.findByAtivoTrue(PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("findByAtivoTrue - deve retornar página vazia quando não há usuários ativos")
    void findByAtivoTrue_semAtivos_deveRetornarPaginaVazia() {
        ReflectionTestUtils.setField(usuarioAtivo, "ativo", false);
        em.persist(usuarioAtivo);
        em.flush();

        Page<Usuario> resultado = usuarioRepository.findByAtivoTrue(PageRequest.of(0, 10));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByAtivoTrue - deve respeitar a paginação")
    void findByAtivoTrue_comVariosAtivos_deveRespeitarPaginacao() {
        em.persist(new Usuario(null, "Carlos", "carlos@email.com", "hash789", true, new HashSet<>()));
        em.persist(new Usuario(null, "Paula",  "paula@email.com",  "hashABC", true, new HashSet<>()));
        em.flush();

        Page<Usuario> pagina0 = usuarioRepository.findByAtivoTrue(PageRequest.of(0, 2));

        assertThat(pagina0.getTotalElements()).isEqualTo(3); // joaoAtivo + carlos + paula
        assertThat(pagina0.getContent()).hasSize(2);
    }
}

