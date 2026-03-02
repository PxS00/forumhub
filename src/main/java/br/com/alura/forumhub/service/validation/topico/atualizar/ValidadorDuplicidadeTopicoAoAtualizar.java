package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDuplicidadeTopicoAoAtualizar implements ValidationAtualizarTopico {

    @Autowired
    private TopicoRepository repository;

    @Override
    public void validar(Topico topico, DadosAtualizacaoTopico dados) {

        boolean duplicado = repository
                .existsByTituloAndMensagemAndIdNot(
                        dados.titulo(),
                        dados.mensagem(),
                        topico.getId()
                );

        if (duplicado) {
            throw new TopicoDuplicadoException();
        }
    }
}
