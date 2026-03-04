package br.com.alura.forumhub.service.validation.usuario.cadastrar;

import br.com.alura.forumhub.dto.usuario.DadosCadastroUsuario;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorEmailDuplicado implements ValidationCadastroUsuario {

    private final UsuarioRepository repository;

    public ValidadorEmailDuplicado(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosCadastroUsuario dados) {
        if (repository.existsByEmailAndAtivoTrue(dados.email())) {
            throw new ValidacaoException("Já existe um usuário cadastrado com este e-mail");
        }
    }
}
