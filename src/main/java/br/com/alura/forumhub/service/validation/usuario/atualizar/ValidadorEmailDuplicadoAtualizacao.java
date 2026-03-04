package br.com.alura.forumhub.service.validation.usuario.atualizar;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEmailDuplicadoAtualizacao implements ValidationAtualizacaoUsuario {

    private final UsuarioRepository repository;

    public ValidadorEmailDuplicadoAtualizacao(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(Usuario usuario, DadosAtualizacaoUsuario dados) {

        if (dados.email() == null) return;

        if (!dados.email().equals(usuario.getEmail())
                && repository.existsByEmailAndAtivoTrue(dados.email())) {

            throw new ValidacaoException("Já existe um usuário ativo com este e-mail");
        }
    }
}
