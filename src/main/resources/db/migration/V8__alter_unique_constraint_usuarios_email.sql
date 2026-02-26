ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuario_email UNIQUE (email);