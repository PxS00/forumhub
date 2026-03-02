package br.com.alura.forumhub.service.validation.topico.excluir;

import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorExcluirTopico implements ValidationExcluirTopico {

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Override
    public void validar(Topico topico) {

        autorizacaoService.validarAutorOuAdmin(topico.getAutor().getId());
    }
}
