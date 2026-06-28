-- Seed das 5 categorias fixas (sem CRUD - briefing v2.0 secao 1.2/3)
-- Idempotente: ON CONFLICT depende da UNIQUE em categoria.nome.
INSERT INTO categoria (nome) VALUES ('IRPF')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('ITR')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('CCIR')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('Consultoria') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('Outros')      ON CONFLICT (nome) DO NOTHING;
