-- Schema do Sistema de Crediario Digital
-- Versionado manualmente (ddl-auto=validate).

-- Categoria (seed fixo - sem CRUD)
CREATE TABLE IF NOT EXISTS categoria (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Cliente (SINGLE_TABLE: tipo = PF | PJ)
CREATE TABLE IF NOT EXISTS cliente (
    id           BIGSERIAL PRIMARY KEY,
    tipo         VARCHAR(2)  NOT NULL,
    nome         VARCHAR(200) NOT NULL,
    sobrenome    VARCHAR(200),
    telefone     VARCHAR(20),
    email        VARCHAR(200),
    conta_gov    VARCHAR(200),
    outros       TEXT,
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    cpf          VARCHAR(14),
    cnpj         VARCHAR(18),
    razao_social VARCHAR(200)
);

-- Servico
CREATE TABLE IF NOT EXISTS servico (
    id           BIGSERIAL PRIMARY KEY,
    descricao    TEXT          NOT NULL,
    data         DATE          NOT NULL,
    valor        NUMERIC(12,2) NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    cliente_id   BIGINT        NOT NULL REFERENCES cliente(id),
    categoria_id BIGINT        NOT NULL REFERENCES categoria(id),
    ativo        BOOLEAN       NOT NULL DEFAULT TRUE
);

-- PagamentoParcial
CREATE TABLE IF NOT EXISTS pagamento_parcial (
    id             BIGSERIAL PRIMARY KEY,
    servico_id     BIGINT        NOT NULL REFERENCES servico(id),
    valor_pago     NUMERIC(12,2) NOT NULL,
    data_pagamento DATE          NOT NULL
);

-- Usuario
CREATE TABLE IF NOT EXISTS usuario (
    id     BIGSERIAL PRIMARY KEY,
    nome   VARCHAR(200) NOT NULL,
    login  VARCHAR(100) NOT NULL UNIQUE,
    senha  VARCHAR(255) NOT NULL,
    perfil VARCHAR(20)  NOT NULL
);

-- LogAcao
CREATE TABLE IF NOT EXISTS log_acao (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT REFERENCES usuario(id),
    acao        VARCHAR(50)  NOT NULL,
    entidade    VARCHAR(100) NOT NULL,
    entidade_id BIGINT,
    timestamp   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
