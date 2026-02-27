package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.usuario.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.dto.usuario.DadosListagemUsuario;
import br.com.alura.forumhub.model.Perfil;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.PerfilRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import br.com.alura.forumhub.service.validation.usuario.atualizar.ValidationAtualizacaoUsuario;
import br.com.alura.forumhub.service.validation.usuario.cadastrar.ValidationCadastroUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    PerfilRepository perfilRepository;

    @Autowired
    private List<ValidationAtualizacaoUsuario> validationAtualizacaoUsuarios;

    @Autowired
    private List<ValidationCadastroUsuario> validationCadastroUsuario;

    private Usuario usuarioExiste(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Usuário não encontrado")
                );
    }

    @Transactional
    public DadosDetalhamentoUsuario cadastrar(DadosCadastroUsuario dados) {

        validationCadastroUsuario.forEach(v -> v.validar(dados));

        String senhaHash = passwordEncoder.encode(dados.senha());

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

        validationAtualizacaoUsuarios
                .forEach(v -> v.validar(id, dados));

        String senhaHash = passwordEncoder.encode(dados.senha());

        usuario.atualizarDados(dados, senhaHash);

        return new DadosDetalhamentoUsuario(usuario);
    }
}
