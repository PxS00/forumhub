CREATE TABLE respostas (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           mensagem TEXT NOT NULL,
                           data_criacao DATETIME NOT NULL,
                           solucao BOOLEAN,

                           topico_id BIGINT NOT NULL,
                           autor_id BIGINT NOT NULL,

                           PRIMARY KEY (id),

                           CONSTRAINT fk_resposta_topico
                               FOREIGN KEY (topico_id)
                                   REFERENCES topicos(id),

                           CONSTRAINT fk_resposta_autor
                               FOREIGN KEY (autor_id)
                                   REFERENCES usuarios(id)
);