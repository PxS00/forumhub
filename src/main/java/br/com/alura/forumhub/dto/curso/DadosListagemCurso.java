package br.com.alura.forumhub.dto.curso;

import br.com.alura.forumhub.model.Curso;

public record DadosListagemCurso(
        Long id,
        String nome,
        String categoria) {

    public DadosListagemCurso(Curso curso) {
        this(curso.getId(), curso.getNome(), curso.getCategoria());
    }
}
