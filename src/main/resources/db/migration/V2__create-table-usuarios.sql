CREATE TABLE usuarios (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL,
                          senha VARCHAR(255) NOT NULL,
                          PRIMARY KEY (id)
);