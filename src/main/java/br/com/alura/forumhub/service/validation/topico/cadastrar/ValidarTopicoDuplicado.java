package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.DadosCadastroTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.repository.TopicoRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidarTopicoDuplicado implements ValidationCadastroTopico {

    @Autowired
    private TopicoRepository repository;

    @Override
    public void validar(DadosCadastroTopico dados) {

        if (repository.existsByTituloAndMensagem(
                dados.titulo(),
                dados.mensagem())) {

            throw new TopicoDuplicadoException();
        }
    }
}
