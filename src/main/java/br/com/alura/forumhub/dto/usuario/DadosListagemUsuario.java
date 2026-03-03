package br.com.alura.forumhub.dto.usuario;

import br.com.alura.forumhub.model.Usuario;

public record DadosListagemUsuario(
        Long id,
        String nome,
        String email,
        Boolean ativo
) {

    public DadosListagemUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getAtivo());
    }
}
