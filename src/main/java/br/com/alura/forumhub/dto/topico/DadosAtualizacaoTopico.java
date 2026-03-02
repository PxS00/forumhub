package br.com.alura.forumhub.dto.topico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoTopico(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 255, message = "Mensagem deve ter no máximo 255 caracteres")
        String mensagem,

        @NotNull(message = "Curso é obrigatório")
        Long idCurso

) {
}
