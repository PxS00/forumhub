package br.com.alura.forumhub.service.validation.resposta.cadastrar;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.springframework.stereotype.Component;

@Component
public class ValidadorTopicoExiste implements ValidationCadastrarResposta {

    private final TopicoRepository repository;

    public ValidadorTopicoExiste(TopicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(DadosCadastroResposta dados) {
        if (!repository.existsById(dados.idTopico())) {
            throw new ValidacaoException("Tópico não encontrado");
        }
    }
}
