-- Esquema inicial PostgreSQL da Soares Recuperadora de Faróis.
-- Crie e selecione o banco "soares_recuperadora" no PostgreSQL antes de executar este arquivo.
-- Este script cria as tabelas; ele não conecta o programa Java ao banco.
--
-- No Java atual, categoria é texto. Ao persistir um produto, a futura camada DAO deve
-- localizar o id_categoria pelo nome da categoria informado no programa.
-- marca e modelo_ano constam nos requisitos do documento, mas ainda não existem na classe Java.

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(40) NOT NULL UNIQUE,
    descricao TEXT
);

CREATE TABLE IF NOT EXISTS produto (
    id_produto INTEGER PRIMARY KEY CHECK (id_produto > 0),
    nome VARCHAR(100) NOT NULL,
    id_categoria INTEGER NOT NULL,
    preco NUMERIC(10, 2) NOT NULL CHECK (preco >= 0),
    quantidade INTEGER NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    descricao VARCHAR(100) NOT NULL,
    marca VARCHAR(100),
    modelo_ano VARCHAR(100),
    CONSTRAINT fk_produto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS orcamento (
    id_orcamento INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_cliente VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    mensagem TEXT NOT NULL
);
