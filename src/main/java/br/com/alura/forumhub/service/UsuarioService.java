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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final List<ValidationAtualizacaoUsuario> validationAtualizacaoUsuarios;
    private final List<ValidationCadastroUsuario> validationCadastroUsuario;
    private final AutorizacaoService autorizacaoService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          PerfilRepository perfilRepository,
                          List<ValidationAtualizacaoUsuario> validationAtualizacaoUsuarios,
                          List<ValidationCadastroUsuario> validationCadastroUsuario,
                          AutorizacaoService autorizacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.validationAtualizacaoUsuarios = validationAtualizacaoUsuarios;
        this.validationCadastroUsuario = validationCadastroUsuario;
        this.autorizacaoService = autorizacaoService;
    }

    private Usuario usuarioExiste(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Usuário não encontrado")
                );
    }

    @Transactional
    public DadosDetalhamentoUsuario cadastrar(DadosCadastroUsuario dados) {

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(dados.email());

        String senhaHash = passwordEncoder.encode(dados.senha());

        if (usuarioExistente.isPresent()) {

            Usuario usuario = usuarioExistente.get();

            if (!usuario.getAtivo()) {
                usuario.reativar(senhaHash);
                return new DadosDetalhamentoUsuario(usuario);
            }

            throw new ValidacaoException("Já existe um usuário cadastrado com este e-mail");
        }

        validationCadastroUsuario.forEach(v -> v.validar(dados));

        Usuario usuario = new Usuario(
                dados.nome(),
                dados.email(),
                senhaHash
        );

        Perfil perfilUser = perfilRepository.findByNome("ROLE_USER");
        usuario.getPerfis().add(perfilUser);

        usuarioRepository.save(usuario);

        return new DadosDetalhamentoUsuario(usuario);
    }

    public Page<DadosListagemUsuario> listar(Pageable paginacao) {

        return usuarioRepository
                .findByAtivoTrue(paginacao)
                .map(DadosListagemUsuario::new);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemUsuario> listarTodos(Pageable paginacao) {

        return usuarioRepository
                .findAll(paginacao)
                .map(DadosListagemUsuario::new);
    }

    public DadosDetalhamentoUsuario detalhar(Long id) {

        Usuario usuario = usuarioExiste(id);

        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizar(Long id, @Valid DadosAtualizacaoUsuario dados) {

        Usuario usuario = usuarioExiste(id);

        autorizacaoService.validarAutorOuAdmin(usuario.getId());

        validationAtualizacaoUsuarios.forEach(v -> v.validar(usuario, dados));

        String senhaHash = passwordEncoder.encode(dados.senha());

        usuario.atualizarDados(dados, senhaHash);

        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioExiste(id);
        autorizacaoService.validarAutorOuAdmin(usuario.getId());
        usuario.desativar();
    }
}
