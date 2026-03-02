package br.com.alura.forumhub.service.validation.resposta.atualizar;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.model.Resposta;

public interface ValidationAtualizarResposta {
    void validar(Resposta resposta, DadosAtualizacaoResposta dados);
}
