package br.com.alura.forumhub.service.validation.resposta.excluir;

import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExcluirResposta implements ValidationExcluirResposta {

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Override
    public void validar(Resposta resposta) {
        autorizacaoService.validarAutorOuAdmin(resposta.getAutor().getId());
    }
}
