package br.com.alura.forumhub.service.validation.usuario.atualizar;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;

public interface ValidationAtualizacaoUsuario {

    void validar(Long id, DadosAtualizacaoUsuario dados);

}
