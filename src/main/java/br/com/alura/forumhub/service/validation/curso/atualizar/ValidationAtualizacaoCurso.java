package br.com.alura.forumhub.service.validation.curso.atualizar;

import br.com.alura.forumhub.dto.curso.DadosAtualizacaoCurso;

public interface ValidationAtualizacaoCurso {

    void validar(Long id, DadosAtualizacaoCurso dados);
}
