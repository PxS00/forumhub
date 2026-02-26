package br.com.alura.forumhub.service.validation.topico.cadastrar;

import br.com.alura.forumhub.dto.topico.DadosCadastroTopico;

public interface ValidationCadastroTopico {
    void validar(DadosCadastroTopico dados);
}
