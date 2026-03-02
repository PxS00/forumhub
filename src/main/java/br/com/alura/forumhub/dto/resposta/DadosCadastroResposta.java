package br.com.alura.forumhub.dto.resposta;

import br.com.alura.forumhub.dto.DadosComAutor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCadastroResposta(

        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
        String mensagem,

        @NotNull(message = "Tópico é obrigatório")
        Long idTopico,

        @NotNull(message = "Autor é obrigatório")
        Long idAutor
) implements DadosComAutor {
}
