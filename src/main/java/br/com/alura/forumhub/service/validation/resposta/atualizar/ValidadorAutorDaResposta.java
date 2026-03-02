package br.com.alura.forumhub.service.validation.resposta.atualizar;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorDaResposta implements ValidationAtualizarResposta {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void validar(Resposta resposta, DadosAtualizacaoResposta dados) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        Usuario usuarioLogado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (!resposta.getAutor().getId().equals(usuarioLogado.getId())) {
            throw new ValidacaoException("Somente o autor pode atualizar a resposta");
        }
    }
}
