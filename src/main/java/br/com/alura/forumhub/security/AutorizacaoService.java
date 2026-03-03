package br.com.alura.forumhub.security;

import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AutorizacaoService {

    public Usuario usuarioLogado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidacaoException("Usuário não autenticado");
        }

        return (Usuario) authentication.getPrincipal();
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

    public void validarAdmin() {
        Usuario usuario = usuarioLogado();

        if (!ehAdmin(usuario)) {
            throw new ValidacaoException("Acesso permitido apenas para administradores");
        }
    }
}
