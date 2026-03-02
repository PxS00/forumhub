package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorTopcio implements ValidationAtualizarTopico {

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Override
    public void validar(Topico topico, DadosAtualizacaoTopico dados) {
        autorizacaoService.validarAutorOuAdmin(topico.getAutor().getId());
    }
}
