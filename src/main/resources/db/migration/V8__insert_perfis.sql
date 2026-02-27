INSERT INTO perfis (nome) VALUES ('ROLE_USER');
INSERT INTO perfis (nome) VALUES ('ROLE_ADMIN');

INSERT INTO usuarios (nome, email, senha)
VALUES (
           'Admin',
           'admin@email.com',
           '$2a$12$QGEa63zWsEcEjk0NCJ7pzuH039OEViHgT0hWAaK8b3nKzFr8VkDUS'
       );

INSERT INTO usuarios_perfis (usuario_id, perfil_id)
VALUES (1, 2);