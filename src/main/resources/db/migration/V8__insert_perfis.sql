INSERT INTO perfis (nome) VALUES ('ROLE_USER');
INSERT INTO perfis (nome) VALUES ('ROLE_ADMIN');

INSERT INTO usuarios (nome, email, senha)
VALUES (
           'Admin',
           'admin@email.com',
           '$2a$10$Dow1z3H1tR6G6x2fXH5QReSgV7t9pZ7C4lE9J1Zr9HkC1l5xY0QyW'
       );

INSERT INTO usuarios_perfis (usuario_id, perfil_id)
VALUES (1, 2);