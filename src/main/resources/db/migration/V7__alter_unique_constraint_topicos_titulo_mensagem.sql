ALTER TABLE topicos
    ADD CONSTRAINT uk_topicos_titulo_mensagem
        UNIQUE (titulo, mensagem);