package br.com.alura.forumhub.service;

import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.usuario.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.model.Perfil;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.PerfilRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    PerfilRepository perfilRepository;

    public DadosDetalhamentoUsuario cadastrar(DadosCadastroUsuario dados) {

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
}
