package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.exception.TopicoDuplicadoException;
import br.com.alura.forumhub.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidarDuplicidadeTopicoAoAtualizar implements ValidationAtualizacaoTopico {

    @Autowired
    private TopicoRepository repository;

    @Override
    public void validar(Long id, DadosAtualizacaoTopico dados) {

        boolean duplicado = repository
                .existsByTituloAndMensagemAndIdNot(
                        dados.titulo(),
                        dados.mensagem(),
                        id
                );

        if (duplicado) {
            throw new TopicoDuplicadoException();
        }
    }
}
