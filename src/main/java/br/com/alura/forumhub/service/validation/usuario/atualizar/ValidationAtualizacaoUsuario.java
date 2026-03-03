package br.com.alura.forumhub.service.validation.usuario.atualizar;

import br.com.alura.forumhub.dto.usuario.DadosAtualizacaoUsuario;
import br.com.alura.forumhub.model.Usuario;

public interface ValidationAtualizacaoUsuario {

    void validar(Usuario usuario, DadosAtualizacaoUsuario dados);

}
