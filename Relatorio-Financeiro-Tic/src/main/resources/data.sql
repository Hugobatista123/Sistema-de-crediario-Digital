-- Seed das 5 categorias fixas (sem CRUD - briefing v2.0 secao 1.2/3)
-- Idempotente: ON CONFLICT depende da UNIQUE em categoria.nome.
INSERT INTO categoria (nome) VALUES ('IRPF')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('ITR')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('CCIR')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('Consultoria') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('Outros')      ON CONFLICT (nome) DO NOTHING;

-- Usuarios seed (senhas hasheadas com BCrypt strength 10)
-- Administrador / admin   -> $2a$10$tSAZUM2nM1Uss.mmbOnb2ejOsMN8GTijDmWPW01EHdqdFIIE22mUq
-- funcionario   / func123 -> $2a$10$ctEwt8Q9v61r3EBBVFbys.JjhZYrq1R1uX1xhXg0CKu08qOxbmu7y

-- Migra login 'admin' -> 'Administrador' e atualiza senha para 'admin' (banco ja existente)
UPDATE usuario
   SET login = 'Administrador',
       nome  = 'Administrador',
       senha = '$2a$10$tSAZUM2nM1Uss.mmbOnb2ejOsMN8GTijDmWPW01EHdqdFIIE22mUq'
 WHERE login = 'admin' AND perfil = 'ADMIN';

-- Insere se nao existir (banco zerado ou primeiro boot apos a correcao)
INSERT INTO usuario (nome, login, senha, perfil) VALUES
    ('Administrador', 'Administrador', '$2a$10$tSAZUM2nM1Uss.mmbOnb2ejOsMN8GTijDmWPW01EHdqdFIIE22mUq', 'ADMIN')
    ON CONFLICT (login) DO NOTHING;
-- Migra hash invalido do funcionario para o hash correto de 'func123'
UPDATE usuario
   SET senha = '$2a$10$ctEwt8Q9v61r3EBBVFbys.JjhZYrq1R1uX1xhXg0CKu08qOxbmu7y'
 WHERE login = 'funcionario' AND perfil = 'FUNCIONARIO';

INSERT INTO usuario (nome, login, senha, perfil) VALUES
    ('Funcionario Padrao', 'funcionario', '$2a$10$ctEwt8Q9v61r3EBBVFbys.JjhZYrq1R1uX1xhXg0CKu08qOxbmu7y', 'FUNCIONARIO')
    ON CONFLICT (login) DO NOTHING;
