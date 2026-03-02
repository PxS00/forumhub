package br.com.alura.forumhub.service.validation.resposta.cadastrar;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExiste implements ValidationCadastrarResposta {

    @Autowired
    private UsuarioRepository repository;

    @Override
    public void validar(DadosCadastroResposta dados) {
        if (!repository.existsById(dados.idAutor())) {
            throw new ValidacaoException("Autor não encontrado");
        }
    }
}
