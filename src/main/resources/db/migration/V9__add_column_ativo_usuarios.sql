-- Adiciona coluna ativo
ALTER TABLE usuarios
    ADD COLUMN ativo BOOLEAN;

-- Define valor padrão como true
UPDATE usuarios
SET ativo = TRUE
WHERE ativo IS NULL;

-- Torna a coluna obrigatória
ALTER TABLE usuarios
    MODIFY COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;