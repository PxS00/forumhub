package br.com.alura.forumhub.service.validation.topico.atualizar;

import br.com.alura.forumhub.dto.topico.DadosAtualizacaoTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorTopcio implements ValidationAtualizarTopico {

    private final AutorizacaoService autorizacaoService;

    public ValidadorAutorTopcio(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @Override
    public void validar(Topico topico, DadosAtualizacaoTopico dados) {
        autorizacaoService.validarAutorOuAdmin(topico.getAutor().getId());
    }
}
