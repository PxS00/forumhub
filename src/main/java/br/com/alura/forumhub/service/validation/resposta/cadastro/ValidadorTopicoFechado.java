package br.com.alura.forumhub.service.validation.resposta.cadastro;

import br.com.alura.forumhub.dto.resposta.DadosCadastroResposta;
import br.com.alura.forumhub.exception.ValidacaoException;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorTopicoFechado implements ValidationCadastroResposta {

    @Autowired
    private TopicoRepository repository;

    @Override
    public void validar(DadosCadastroResposta dados) {

        Topico topico = repository.findById(dados.idTopico())
                .orElseThrow(() -> new ValidacaoException("Tópico não encontrado"));

        if (topico.getStatus() == StatusTopico.FECHADO) {
            throw new ValidacaoException("Não é permitido responder um tópico fechado");
        }
    }
}
