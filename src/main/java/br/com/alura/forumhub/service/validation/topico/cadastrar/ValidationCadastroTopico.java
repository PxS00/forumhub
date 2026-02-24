package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.DadosCadastroTopico;

public interface ValidationCadastroTopico {
    void validar(DadosCadastroTopico dados);
}
