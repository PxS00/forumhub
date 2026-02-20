CREATE TABLE topicos (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         titulo VARCHAR(255) NOT NULL,
                         mensagem TEXT NOT NULL,
                         data_criacao DATETIME NOT NULL,
                         status VARCHAR(50) NOT NULL,

                         autor_id BIGINT NOT NULL,
                         curso_id BIGINT NOT NULL,

                         PRIMARY KEY (id),

                         CONSTRAINT fk_topico_autor
                             FOREIGN KEY (autor_id)
                                 REFERENCES usuarios(id),

                         CONSTRAINT fk_topico_curso
                             FOREIGN KEY (curso_id)
                                 REFERENCES cursos(id)
);