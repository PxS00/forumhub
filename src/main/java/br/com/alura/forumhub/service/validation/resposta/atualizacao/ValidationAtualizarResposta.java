package br.com.alura.forumhub.service.validation.resposta.atualizacao;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;

public interface ValidationAtualizarResposta {
    void validar(Long id, DadosAtualizacaoResposta dados);
}
