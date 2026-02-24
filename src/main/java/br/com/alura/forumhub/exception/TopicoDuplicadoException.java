package br.com.alura.forumhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TopicoDuplicadoException extends RuntimeException {

    public TopicoDuplicadoException() {
        super("Tópico já cadastrado com esse título e mensagem");
    }
}
