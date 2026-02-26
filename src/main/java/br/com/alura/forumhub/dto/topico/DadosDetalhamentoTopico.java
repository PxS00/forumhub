package br.com.alura.forumhub.dto.topico;

import br.com.alura.forumhub.model.Topico;

public record DadosDetalhamentoTopico(
        Long id,
        String titulo,
        String mensagem,
        String nomeAutor,
        String nomeCurso
) {
    public DadosDetalhamentoTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensagem(),
                topico.getAutor().getNome(),
                topico.getCurso().getNome()
        );
    }
}
