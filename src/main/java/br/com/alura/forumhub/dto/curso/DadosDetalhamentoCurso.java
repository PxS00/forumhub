package br.com.alura.forumhub.dto.curso;

import br.com.alura.forumhub.model.Curso;

public record DadosDetalhamentoCurso(
        Long id,
        String nome,
        String categoria
) {
    public DadosDetalhamentoCurso(Curso curso) {
        this(
                curso.getId(),
                curso.getNome(),
                curso.getCategoria()
        );
    }
}
