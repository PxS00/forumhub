package br.com.alura.forumhub.security;

import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de SecurityFilter")
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository repository;

    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = new Usuario(1L, "Ana Silva", "ana@email.com", "hashed", true, new HashSet<>());
        usuarioInativo = new Usuario(2L, "Bob", "bob@email.com", "hashed", false, new HashSet<>());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar usuário ativo quando token é válido")
    void doFilterInternal_tokenValidoUsuarioAtivo_deveAutenticar() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        given(tokenService.getSubject("valid-token")).willReturn("ana@email.com");
        given(repository.findByEmail("ana@email.com")).willReturn(Optional.of(usuarioAtivo));

        securityFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(usuarioAtivo);
    }

    @Test
    @DisplayName("Não deve autenticar usuário inativo mesmo com token válido")
    void doFilterInternal_tokenValidoUsuarioInativo_naoDeveAutenticar() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        given(tokenService.getSubject("valid-token")).willReturn("bob@email.com");
        given(repository.findByEmail("bob@email.com")).willReturn(Optional.of(usuarioInativo));

        securityFilter.doFilterInternal(request, response, chain);

        // Usuário inativo: SecurityContext deve permanecer vazio
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Não deve autenticar quando não há header Authorization")
    void doFilterInternal_semHeader_naoDeveAutenticar() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(tokenService).should(never()).getSubject(any());
    }

    @Test
    @DisplayName("Não deve autenticar quando header Authorization não começa com Bearer")
    void doFilterInternal_headerSemBearer_naoDeveAutenticar() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(tokenService).should(never()).getSubject(any());
    }

    @Test
    @DisplayName("Deve sempre passar a requisição adiante na cadeia mesmo sem token")
    void doFilterInternal_semToken_devePassarParaProximoFiltro() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, chain);

        // Verifica que a cadeia foi invocada (request chegou ao próximo filtro)
        assertThat(chain.getRequest()).isNotNull();
    }
}

