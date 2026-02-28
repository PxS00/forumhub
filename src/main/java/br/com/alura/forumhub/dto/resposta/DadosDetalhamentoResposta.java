package br.com.alura.forumhub.dto.resposta;

import br.com.alura.forumhub.model.Resposta;

import java.time.LocalDateTime;

public record DadosDetalhamentoResposta(
        Long id,
        String mensagem,
        LocalDateTime dataCriacao,
        Boolean solucao,
        Long idTopico,
        Long idAutor
) {
    public DadosDetalhamentoResposta(Resposta resposta) {
        this(
                resposta.getId(),
                resposta.getMensagem(),
                resposta.getDataCriacao(),
                resposta.getSolucao(),
                resposta.getTopico().getId(),
                resposta.getAutor().getId()
        );
    }
}
