package br.com.alura.forumhub.service.validation.usuario.cadastrar;

import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEmailDuplicado implements ValidationCadastroUsuario {

    @Autowired
    UsuarioRepository repository;

    @Override
    public void validar(DadosCadastroUsuario dados) {
        if (repository.existsByEmail(dados.email())) {
            throw new ValidacaoException("Email já cadastrado");
        }
    }
}
