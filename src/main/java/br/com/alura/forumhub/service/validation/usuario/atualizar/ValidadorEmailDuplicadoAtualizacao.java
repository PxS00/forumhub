package br.com.alura.forumhub.service.validation.usuario.atualizar;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEmailDuplicadoAtualizacao implements ValidationAtualizacaoUsuario {

    @Autowired
    UsuarioRepository repository;

    @Override
    public void validar(Long id, DadosAtualizacaoUsuario dados) {

        if (dados.email() == null) return;

        var usuario = repository.findById(id).orElseThrow();

        if (!dados.email().equals(usuario.getEmail())
                && repository.existsByEmail(dados.email())) {

            throw new ValidacaoException("E-mail já cadastrado");
        }

    }
}
