package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Perfil;
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
@DisplayName("Testes de repositório de Perfil")
class PerfilRepositoryTest {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        em.persist(new Perfil(null, "ROLE_USER"));
        em.persist(new Perfil(null, "ROLE_ADMIN"));
        em.flush();
    }

    // =========================================================
    // findByNome
    // =========================================================

    @Test
    @DisplayName("findByNome - deve retornar o perfil quando nome existe")
    void findByNome_nomeExistente_deveRetornarPerfil() {
        Perfil perfil = perfilRepository.findByNome("ROLE_USER");

        assertThat(perfil).isNotNull();
        assertThat(perfil.getNome()).isEqualTo("ROLE_USER");
        assertThat(perfil.getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("findByNome - deve retornar o perfil de admin quando nome existe")
    void findByNome_nomeAdmin_deveRetornarPerfilAdmin() {
        Perfil perfil = perfilRepository.findByNome("ROLE_ADMIN");

        assertThat(perfil).isNotNull();
        assertThat(perfil.getNome()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("findByNome - deve retornar null quando nome não existe")
    void findByNome_nomeInexistente_deveRetornarNull() {
        Perfil perfil = perfilRepository.findByNome("ROLE_INEXISTENTE");

        assertThat(perfil).isNull();
    }

    @Test
    @DisplayName("findByNome - deve ser case-sensitive")
    void findByNome_nomeDiferenteMaiusculas_deveRetornarNull() {
        Perfil perfil = perfilRepository.findByNome("role_user");

        assertThat(perfil).isNull();
    }
}

