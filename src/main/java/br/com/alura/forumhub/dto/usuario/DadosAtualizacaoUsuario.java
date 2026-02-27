package br.com.alura.forumhub.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoUsuario(

        @Size(min = 3, max = 100,
                message = "Nome deve ter entre 3 e 100 caracteres")
        @Pattern(
                regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ ]+$",
                message = "Nome deve conter apenas letras e espaços"
        )
        String nome,

        @Email(message = "E-mail inválido")
        String email,

        @Size(min = 8, max = 64,
                message = "Senha deve ter entre 8 e 64 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-])[A-Za-z\\d@$!%*?&.#_\\-]+$",
                message = "Senha deve conter letra maiúscula, minúscula, número e símbolo"
        )
        String senha
) {
}
