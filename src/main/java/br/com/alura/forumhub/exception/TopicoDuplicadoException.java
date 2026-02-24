package br.com.alura.forumhub.exception;

public class TopicoDuplicadoException extends RuntimeException {

    public TopicoDuplicadoException() {
        super("Tópico já cadastrado com esse título e mensagem");
    }
}
