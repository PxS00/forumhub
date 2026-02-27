package br.com.alura.forumhub.dto.topico;

import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;

import java.time.LocalDateTime;

public record DadosListagemTopico(

        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        StatusTopico status,
        String nomeAutor,
        String nomeCurso

) {
    public DadosListagemTopico(Topico topico) {
        this(
                topico.getTitulo(),
                topico.getMensagem(),
                topico.getDataCriacao(),
                topico.getStatus(),
                topico.getAutor().getNome(),
                topico.getCurso().getNome()
        );
    }
}
