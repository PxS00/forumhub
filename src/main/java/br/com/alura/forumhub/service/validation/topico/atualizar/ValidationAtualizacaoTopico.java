package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;

public interface ValidationAtualizacaoTopico {
    void validar(Long id, DadosAtualizacaoTopico dados);
}
