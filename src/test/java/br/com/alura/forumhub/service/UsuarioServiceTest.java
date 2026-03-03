package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.usuario.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosListagemUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Perfil;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.PerfilRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.security.AutorizacaoService;
import br.com.alura.forumhub.service.validation.usuario.atualizar.ValidationAtualizacaoUsuario;
import br.com.alura.forumhub.service.validation.usuario.cadastrar.ValidationCadastroUsuario;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Testes unitários de UsuarioService")
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private AutorizacaoService autorizacaoService;

    @Mock
    private ValidationCadastroUsuario validadorCadastro;

    @Mock
    private ValidationAtualizacaoUsuario validadorAtualizacao;

    private Usuario usuarioFake;
    private Perfil perfilUserFake;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "validationCadastroUsuario", List.of(validadorCadastro));
        ReflectionTestUtils.setField(service, "validationAtualizacaoUsuarios", List.of(validadorAtualizacao));

        perfilUserFake = new Perfil(1L, "ROLE_USER");
        usuarioFake = new Usuario(1L, "Ana Silva", "ana@email.com", "hashed123", true, new HashSet<>());
    }

    // =========================================================
    // CADASTRAR
    // =========================================================

    @Test
    @DisplayName("Deve cadastrar usuário novo com sucesso")
    void cadastrar_usuarioNovo_deveRetornarDetalhamento() {
        var dados = new DadosCadastroUsuario("Ana Silva", "ana@email.com", "Senha@123");

        given(usuarioRepository.findByEmail("ana@email.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("Senha@123")).willReturn("hashed123");
        willDoNothing().given(validadorCadastro).validar(dados);
        given(perfilRepository.findByNome("ROLE_USER")).willReturn(perfilUserFake);
        given(usuarioRepository.save(any(Usuario.class))).willAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1L);
            return u;
        });

        DadosDetalhamentoUsuario resultado = service.cadastrar(dados);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("Ana Silva");
        assertThat(resultado.email()).isEqualTo("ana@email.com");
        then(usuarioRepository).should().save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve reativar usuário inativo com mesmo e-mail")
    void cadastrar_usuarioInativo_deveReativarUsuario() {
        var dados = new DadosCadastroUsuario("Ana Silva", "ana@email.com", "NovaSenha@1");
        Usuario usuarioInativo = new Usuario(1L, "Ana Silva", "ana@email.com", "oldHash", false, new HashSet<>());

        given(usuarioRepository.findByEmail("ana@email.com")).willReturn(Optional.of(usuarioInativo));
        given(passwordEncoder.encode("NovaSenha@1")).willReturn("newHash");

        DadosDetalhamentoUsuario resultado = service.cadastrar(dados);

        assertThat(resultado).isNotNull();
        assertThat(usuarioInativo.getAtivo()).isTrue();
        then(usuarioRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando e-mail já está em uso por usuário ativo")
    void cadastrar_emailDuplicado_deveLancarValidacaoException() {
        var dados = new DadosCadastroUsuario("Outro", "ana@email.com", "Senha@123");
        Usuario usuarioAtivo = new Usuario(2L, "Outro", "ana@email.com", "hash", true, new HashSet<>());

        given(usuarioRepository.findByEmail("ana@email.com")).willReturn(Optional.of(usuarioAtivo));
        given(passwordEncoder.encode(anyString())).willReturn("hash");

        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail");
    }

    // =========================================================
    // LISTAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar apenas usuários ativos")
    void listar_deveRetornarApenasAtivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> pagina = new PageImpl<>(List.of(usuarioFake));
        given(usuarioRepository.findByAtivoTrue(pageable)).willReturn(pagina);

        Page<DadosListagemUsuario> resultado = service.listar(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Ana Silva");
        assertThat(resultado.getContent().get(0).ativo()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar todos os usuários (admin)")
    void listarTodos_deveRetornarTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuarioInativo = new Usuario(2L, "Bob", "bob@email.com", "hash", false, new HashSet<>());
        Page<Usuario> pagina = new PageImpl<>(List.of(usuarioFake, usuarioInativo));
        given(usuarioRepository.findAll(pageable)).willReturn(pagina);

        Page<DadosListagemUsuario> resultado = service.listarTodos(pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(2);
    }

    // =========================================================
    // DETALHAR
    // =========================================================

    @Test
    @DisplayName("Deve retornar detalhamento do usuário quando ID válido")
    void detalhar_idValido_deveRetornarDetalhamento() {
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuarioFake));

        DadosDetalhamentoUsuario resultado = service.detalhar(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Ana Silva");
        assertThat(resultado.email()).isEqualTo("ana@email.com");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando usuário não encontrado pelo ID")
    void detalhar_idInexistente_deveLancarEntityNotFoundException() {
        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    // =========================================================
    // ATUALIZAR
    // =========================================================

    @Test
    @DisplayName("Deve atualizar usuário com sucesso quando dados válidos")
    void atualizar_comDadosValidos_deveRetornarUsuarioAtualizado() {
        var dados = new DadosAtualizacaoUsuario("Ana Souza", "ana.souza@email.com", "NovaSenha@1");

        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuarioFake));
        willDoNothing().given(autorizacaoService).validarAutorOuAdmin(1L);
        willDoNothing().given(validadorAtualizacao).validar(usuarioFake, dados);
        given(passwordEncoder.encode("NovaSenha@1")).willReturn("newHash");

        DadosDetalhamentoUsuario resultado = service.atualizar(1L, dados);

        assertThat(resultado.nome()).isEqualTo("Ana Souza");
        assertThat(resultado.email()).isEqualTo("ana.souza@email.com");
        then(validadorAtualizacao).should().validar(usuarioFake, dados);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar usuário inexistente")
    void atualizar_idInexistente_deveLancarEntityNotFoundException() {
        var dados = new DadosAtualizacaoUsuario("Nome", "email@test.com", "Senha@123");
        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(999L, dados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário não autorizado ao atualizar")
    void atualizar_usuarioNaoAutorizado_deveLancarValidacaoException() {
        var dados = new DadosAtualizacaoUsuario("Nome", "email@test.com", "Senha@123");

        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuarioFake));
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(autorizacaoService).validarAutorOuAdmin(1L);

        assertThatThrownBy(() -> service.atualizar(1L, dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");
    }

    // =========================================================
    // DELETAR (desativar)
    // =========================================================

    @Test
    @DisplayName("Deve desativar usuário com sucesso quando ID válido")
    void deletar_idValido_deveDesativarUsuario() {
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuarioFake));
        willDoNothing().given(autorizacaoService).validarAutorOuAdmin(1L);

        assertThatCode(() -> service.deletar(1L)).doesNotThrowAnyException();

        assertThat(usuarioFake.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar usuário inexistente")
    void deletar_idInexistente_deveLancarEntityNotFoundException() {
        given(usuarioRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando usuário não autorizado ao deletar")
    void deletar_usuarioNaoAutorizado_deveLancarValidacaoException() {
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuarioFake));
        willThrow(new ValidacaoException("Usuário não autorizado"))
                .given(autorizacaoService).validarAutorOuAdmin(1L);

        assertThatThrownBy(() -> service.deletar(1L))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Usuário não autorizado");
    }
}

