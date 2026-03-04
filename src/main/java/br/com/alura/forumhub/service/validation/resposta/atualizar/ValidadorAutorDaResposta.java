package br.com.alura.forumhub.service.validation.resposta.atualizar;

import br.com.alura.forumhub.dto.resposta.DadosAtualizacaoResposta;
import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.security.AutorizacaoService;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAutorDaResposta implements ValidationAtualizarResposta {

    private final AutorizacaoService autorizacaoService;

    public ValidadorAutorDaResposta(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @Override
    public void validar(Resposta resposta, DadosAtualizacaoResposta dados) {

        autorizacaoService.validarAutorOuAdmin(resposta.getAutor().getId());
    }
}
