package br.com.alura.forumhub.security;

import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AutorizacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario usuarioLogado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public boolean ehAdmin(Usuario usuario) {
        return usuario.getPerfis()
                .stream()
                .anyMatch(p -> p.getNome().equals("ROLE_ADMIN"));
    }

    public void validarAutorOuAdmin(Long idAutor) {
        Usuario usuario = usuarioLogado();

        if (ehAdmin(usuario)) {
            return;
        }

        if (!usuario.getId().equals(idAutor)) {
            throw new ValidacaoException("Usuário não autorizado");
        }
    }
}
