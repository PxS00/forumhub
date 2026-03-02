package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.model.Topico;

public interface ValidationAtualizarTopico {
    void validar(Topico topico, DadosAtualizacaoTopico dados);
}
