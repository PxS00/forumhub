package br.com.alura.forumhub.service.validation.topico.excluir;

import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExcluirTopico implements ValidationExcluirTopico {

    private final AutorizacaoService autorizacaoService;

    public ValidadorAutorExcluirTopico(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @Override
    public void validar(Topico topico) {

        autorizacaoService.validarAutorOuAdmin(topico.getAutor().getId());
    }
}
