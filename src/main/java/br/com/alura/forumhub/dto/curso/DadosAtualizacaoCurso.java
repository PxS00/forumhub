package br.com.alura.forumhub.dto.curso;

import jakarta.validation.constraints.NotBlank;

public record DadosAtualizacaoCurso(
        @NotBlank
        String nome,
        @NotBlank
        String categoria
) {
}
