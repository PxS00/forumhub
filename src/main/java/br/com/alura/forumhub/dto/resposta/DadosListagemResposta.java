package br.com.alura.forumhub.dto.resposta;

import br.com.alura.forumhub.model.Resposta;

public record DadosListagemResposta(
        Long id,
        String mensagem,
        String nomeAutor,
        String tituloTopico
) {
    public DadosListagemResposta(Resposta resposta) {
        this(resposta.getId(), resposta.getMensagem(), resposta.getAutor().getNome(), resposta.getTopico().getTitulo());
    }
}
