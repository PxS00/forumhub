package br.com.alura.forumhub.dto.usuario;

import br.com.alura.forumhub.model.Usuario;

public record DadosDetalhamentoUsuario(
        Long id,
        String nome,
        String email
) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
