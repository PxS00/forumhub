package br.com.alura.forumhub.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacao(

        @NotBlank
        String email,

        @NotBlank
        String senha
) {}
