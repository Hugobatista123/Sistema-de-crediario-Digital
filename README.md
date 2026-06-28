# Sistema de Crediario Digital

Sistema web interno do **Escritorio Contabil Atual LTDA** para controle de
servicos prestados, cobrancas e pagamentos parciais. Substitui o controle
manual em caderno fisico.

> **Stack:** Java 17 + Spring Boot 3.2.5 + Maven | PostgreSQL 14+ | HTML + CSS + JavaScript puro (zero frameworks frontend)

---

## Pre-requisitos

- **Java 17** (JDK)
- **Maven 3.8+**
- **PostgreSQL 14+** rodando em `localhost:5432`
- Banco de dados `crediario` criado (ver abaixo)

---

## Configuracao inicial

### 1. Criar o banco de dados

```sql
CREATE DATABASE crediario;
```

O schema (`schema.sql`) e o seed de categorias (`data.sql`) sao aplicados
automaticamente no startup pela configuracao `spring.sql.init.mode=always`.

### 2. Variavel de ambiente para a senha do banco

A senha **nunca** e gravada em `application.properties`. Use:

**PowerShell (Windows):**
```powershell
$env:DB_PASSWORD = "sua_senha_aqui"
```

**Bash (Linux/macOS):**
```bash
export DB_PASSWORD="sua_senha_aqui"
```

---

## Como executar

```bash
# Compilar e testar
mvn clean install

# Subir a aplicacao
mvn spring-boot:run
```

Acesse: `http://localhost:8080`

---

## Estrutura do projeto

```
src/
  main/
    java/br/com/crediario/
      CrediarioApplication.java
      config/
        CorsConfig.java                    # CORS liberado para desenvolvimento
        SecurityConfig.java                # Autenticacao por sessao, perfis ADMIN/FUNCIONARIO
      controller/
        CategoriaController.java
        ClienteController.java
        GlobalExceptionHandler.java
        ServicoController.java
        PagamentoParcialController.java
        InadimplentesController.java
        RelatorioController.java
        AuthController.java
        LogController.java
      service/
        ClienteService.java
        ServicoService.java
        PagamentoParcialService.java
        RelatorioService.java
        UsuarioService.java
        LogAcaoService.java
      repository/
        CategoriaRepository.java
        ClienteRepository.java
        ServicoRepository.java
        PagamentoParcialRepository.java
        UsuarioRepository.java
        LogAcaoRepository.java
      dto/
        ClienteRequest.java
        ClientePFRequest.java
        ClientePJRequest.java
        ClienteResponse.java
        ServicoRequest.java
        ServicoStatusRequest.java
        ServicoResponse.java
        PagamentoParcialRequest.java
        PagamentoParcialResponse.java
        FaturamentoResponse.java
        HistoricoClienteResponse.java
        InadimplentesResponse.java
        ClienteInadimplente.java
        ContadorResponse.java
        TotaisStatusResponse.java
        ItemFaturamento.java
        ServicoComPagamentos.java
        LoginRequest.java
        LoginResponse.java
        LogAcaoResponse.java
      model/
        Categoria.java
        Cliente.java                       # SINGLE_TABLE, abstrata
        ClientePF.java                     # discriminador PF
        ClientePJ.java                     # discriminador PJ
        StatusCobranca.java                # enum: A_COBRAR / PENDENTE / PAGO
        Servico.java
        PagamentoParcial.java
        PerfilUsuario.java                 # enum: ADMIN / FUNCIONARIO
        Usuario.java                       # implements UserDetails
        LogAcao.java
    resources/
      application.properties
      schema.sql                           # DDL de todas as tabelas
      data.sql                             # Seed: categorias e usuarios
      static/
        index.html
        login.html
        css/style.css
        js/
          api.js                           # Abstracão fetch (get/post/put/del)
          auth.js                          # Interceptor de autenticacao
          login.js                         # Formulario de login
          clientes.js
          servicos.js
          pagamentos.js
          dashboard.js
          inadimplentes.js
          relatorios.js
          logs.js                          # Tabela de auditoria (ADMIN)
  test/
    java/br/com/crediario/service/
      ClienteServiceTest.java
      ServicoServiceTest.java
      PagamentoParcialServiceTest.java
      RelatorioServiceTest.java
      UsuarioServiceTest.java
      LogAcaoServiceTest.java
```

---

## Schema do banco

```sql
-- Tabelas presentes no schema.sql (nao modificar manualmente)
categoria         -- id, nome
cliente           -- SINGLE_TABLE: PF (cpf) e PJ (cnpj, razao_social)
servico           -- id, descricao, data, valor, status, cliente_id, categoria_id, ativo
pagamento_parcial -- id, servico_id, valor_pago, data_pagamento
usuario           -- autenticacao
log_acao          -- auditoria de operacoes de escrita
```

---

## Controllers

### `CategoriaController` — `/api/categorias`

Acessa o repositorio diretamente (sem camada de service) por se tratar de dados de referencia somente leitura.

| Metodo | Endpoint        | Descricao                                                                        | Perfil      | Status |
|--------|-----------------|----------------------------------------------------------------------------------|-------------|--------|
| GET    | /api/categorias | Lista todas as categorias (`IRPF`, `ITR`, `CCIR`, `Consultoria`, `Outros`)      | Autenticado | 200    |

---

### `ClienteController` — `/api/clientes`

Gerencia o ciclo de vida de clientes (PF e PJ). O endpoint `GET /{id}` injeta os servicos ativos do cliente na resposta via `ServicoService`.

| Metodo | Endpoint                     | Descricao                                                          | Perfil      | Status |
|--------|------------------------------|--------------------------------------------------------------------|-------------|--------|
| GET    | /api/clientes                | Lista clientes ativos ordenados por nome; aceita `?nome=` para busca | Autenticado | 200 |
| GET    | /api/clientes/{id}           | Busca cliente por ID com lista de servicos ativos                  | Autenticado | 200    |
| GET    | /api/clientes/excluidos      | Lista clientes com soft delete (`ativo=false`)                     | Autenticado | 200    |
| POST   | /api/clientes                | Cadastra cliente PF ou PJ                                          | Autenticado | 201    |
| PUT    | /api/clientes/{id}           | Atualiza dados do cliente                                          | Autenticado | 200    |
| PUT    | /api/clientes/{id}/restaurar | Reativa cliente excluido (`ativo=true`)                            | Autenticado | 200    |
| DELETE | /api/clientes/{id}           | Soft delete (`ativo=false`, sem remocao fisica)                    | Autenticado | 204    |

---

### `ServicoController` — `/api`

Gerencia servicos vinculados a clientes. Todos os endpoints de escrita registram log de auditoria via `LogAcaoService`.

| Metodo | Endpoint                      | Descricao                                                                         | Perfil      | Status |
|--------|-------------------------------|-----------------------------------------------------------------------------------|-------------|--------|
| GET    | /api/clientes/{id}/servicos   | Lista servicos ativos do cliente, ordenados por data desc                         | Autenticado | 200    |
| POST   | /api/clientes/{id}/servicos   | Registra novo servico (descricao, data, valor, categoriaId obrigatorios)          | Autenticado | 201    |
| GET    | /api/servicos/{id}            | Busca servico por ID com saldo devedor calculado                                  | Autenticado | 200    |
| GET    | /api/servicos/excluidos       | Lista servicos com soft delete                                                    | Autenticado | 200    |
| PUT    | /api/servicos/{id}            | Atualiza descricao, data e/ou valor do servico                                    | Autenticado | 200    |
| PUT    | /api/servicos/{id}/status     | Altera status conforme fluxo validado (ver Regras de negocio)                     | Autenticado | 200    |
| PUT    | /api/servicos/{id}/restaurar  | Reativa servico excluido                                                          | Autenticado | 200    |
| DELETE | /api/servicos/{id}            | Soft delete do servico                                                            | Autenticado | 204    |

---

### `PagamentoParcialController` — `/api`

Registra e consulta pagamentos parciais de um servico. O registro e permitido apenas para servicos com status `PENDENTE`.

| Metodo | Endpoint                       | Descricao                                                                              | Perfil      | Status |
|--------|--------------------------------|----------------------------------------------------------------------------------------|-------------|--------|
| GET    | /api/servicos/{id}/pagamentos  | Lista todos os pagamentos parciais do servico                                          | Autenticado | 200    |
| POST   | /api/servicos/{id}/pagamentos  | Registra pagamento parcial; se saldo zerar, status vai para `PAGO` automaticamente    | Autenticado | 201    |

---

### `RelatorioController` — `/api/relatorios`

Endpoints restritos a perfil `ADMIN`. Todos os parametros obrigatorios sao validados antes de delegar ao `RelatorioService`.

| Metodo | Endpoint                      | Parametros                 | Descricao                                                                                             | Status |
|--------|-------------------------------|----------------------------|-------------------------------------------------------------------------------------------------------|--------|
| GET    | /api/relatorios/faturamento   | `inicio`, `fim` (ISO date) | Servicos `PAGO` no periodo com total acumulado. Rejeita datas nulas ou invertidas.                   | 200    |
| GET    | /api/relatorios/cliente/{id}  | `id` (path)                | Historico completo do cliente: dados + servicos + pagamentos parciais + saldo devedor                 | 200    |
| GET    | /api/relatorios/inadimplencia | —                          | Inadimplentes em objeto envelope `{"inadimplentes":[...]}` (formato alternativo ao `/api/inadimplentes`) | 200 |
| GET    | /api/relatorios/totais        | `status` (string)          | Soma e contagem de servicos ativos pelo status (`A_COBRAR`, `PENDENTE` ou `PAGO`)                    | 200    |

---

### `InadimplentesController` — `/api/inadimplentes`

Compartilha o `RelatorioService` com o `RelatorioController`, mas retorna formato diferente: array plano em vez de objeto envelope. Acessivel por `ADMIN` e `FUNCIONARIO`.

| Metodo | Endpoint                | Descricao                                                                                         | Perfil              | Status |
|--------|-------------------------|---------------------------------------------------------------------------------------------------|---------------------|--------|
| GET    | /api/inadimplentes      | Array de clientes com saldo devedor em servicos `PENDENTE` e ativos, ordenados por debito desc    | ADMIN / FUNCIONARIO | 200    |
| GET    | /api/inadimplentes/count | Quantidade numerica de clientes inadimplentes                                                    | ADMIN / FUNCIONARIO | 200    |

---

### `AuthController` — `/api/auth`

Autentica usuarios via Spring Security com sessao HTTP stateful (sem JWT). Erros de credencial retornam mensagem generica para evitar enumeracao de usuarios.

| Metodo | Endpoint         | Body                  | Descricao                                                                           | Acesso      | Status    |
|--------|------------------|-----------------------|-------------------------------------------------------------------------------------|-------------|-----------|
| POST   | /api/auth/login  | `{"login","senha"}`   | Autentica, salva contexto na sessao HTTP e retorna nome e perfil do usuario         | Publico     | 200 / 401 |
| POST   | /api/auth/logout | —                     | Invalida sessao HTTP e limpa `SecurityContextHolder`                                | Publico     | 200       |
| GET    | /api/auth/me     | —                     | Retorna `nome`, `login` e `perfil` do usuario da sessao ativa                      | Autenticado | 200 / 401 |

---

### `LogController` — `/api/logs`

Acessa o repositorio diretamente (sem camada de service) para listar os registros de auditoria. Restrito a `ADMIN`.

| Metodo | Endpoint  | Descricao                                                                     | Perfil | Status |
|--------|-----------|-------------------------------------------------------------------------------|--------|--------|
| GET    | /api/logs | Lista todos os logs de acao ordenados do mais recente para o mais antigo      | ADMIN  | 200    |

---

### `GlobalExceptionHandler`

Intercepta excecoes lancadas em qualquer controller e retorna JSON estruturado `{"erro": "mensagem"}`.

| Excecao capturada          | HTTP | Casos de uso tipicos                                                    |
|----------------------------|------|-------------------------------------------------------------------------|
| `IllegalArgumentException` | 400  | Parametros ausentes ou invalidos (datas nulas, status desconhecido)     |
| `IllegalStateException`    | 409  | Conflito de estado (transicao de status invalida, pagamento excedente)  |
| `NoSuchElementException`   | 404  | Entidade nao encontrada no banco                                        |
| `Exception` (generica)     | 500  | Qualquer excecao nao mapeada; mensagem fixa para nao expor detalhes internos |

---

## Services

### `ClienteService`

Gerencia o CRUD de clientes com suporte a soft delete. Toda operacao de escrita registra log de auditoria via `LogAcaoService`, obtendo o login do usuario autenticado pelo `SecurityContextHolder` sem alterar as assinaturas dos metodos publicos.

| Metodo            | Descricao                                                                                                                |
|-------------------|--------------------------------------------------------------------------------------------------------------------------|
| `listar`          | Retorna clientes ativos ordenados por nome (A-Z).                                                                        |
| `buscarPorNome`   | Busca case-insensitive pelo campo nome entre clientes ativos.                                                            |
| `buscarPorId`     | Retorna cliente por ID; lanca `NoSuchElementException` se nao encontrado.                                               |
| `salvar`          | Valida nome obrigatorio, cria entidade `ClientePF` ou `ClientePJ` conforme tipo, persiste e registra log `CADASTRAR_CLIENTE`. |
| `atualizar`       | Atualiza apenas os campos informados (patch parcial); registra log `ATUALIZAR_CLIENTE`.                                  |
| `remover`         | Soft delete (`ativo=false`); registra log `REMOVER_CLIENTE`.                                                            |
| `restaurar`       | Reativa cliente (`ativo=true`); registra log `RESTAURAR_CLIENTE`.                                                       |
| `listarExcluidos` | Retorna clientes com `ativo=false` ordenados por nome.                                                                   |

---

### `ServicoService`

Gerencia o ciclo de vida de servicos, incluindo calculo de saldo devedor e validacao de transicoes de status. Registra log de auditoria em todas as operacoes de escrita.

| Metodo                    | Descricao                                                                                                              |
|---------------------------|------------------------------------------------------------------------------------------------------------------------|
| `listarPorCliente`        | Retorna servicos ativos de um cliente ordenados por data desc, cada um com saldo devedor calculado.                    |
| `buscarPorId`             | Retorna servico por ID com saldo devedor.                                                                              |
| `registrar`               | Valida campos obrigatorios (descricao, data, valor nao-negativo, categoriaId), vincula ao cliente e categoria, registra log `CADASTRAR_SERVICO`. |
| `atualizar`               | Patch parcial de descricao, data e valor; registra log `ATUALIZAR_SERVICO`.                                           |
| `alterarStatus`           | Valida transicao (`A_COBRAR→PENDENTE` ou `PENDENTE→PAGO`); lanca `IllegalStateException` se invalida; registra log `ALTERAR_STATUS_SERVICO`. |
| `remover`                 | Soft delete; registra log `REMOVER_SERVICO`.                                                                          |
| `restaurar`               | Reativa servico; registra log `RESTAURAR_SERVICO`.                                                                    |
| `listarExcluidos`         | Retorna servicos com `ativo=false`.                                                                                    |
| `calcularSaldoDevedor`    | Metodo publico que delega para `calcularSaldo` (util para consultas externas).                                        |
| `calcularSaldo` *(pkg)*   | Soma os `valorPago` dos pagamentos parciais e subtrai do valor do servico; retorna `0` se negativo.                   |

---

### `PagamentoParcialService`

Controla o registro de pagamentos parciais com validacao de regras de negocio.

| Metodo             | Descricao                                                                                                                         |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `listarPorServico` | Retorna todos os pagamentos parciais de um servico.                                                                               |
| `registrar`        | Valida que o servico esta `PENDENTE`, calcula novo saldo apos o pagamento, rejeita se exceder o saldo devedor (`IllegalStateException`), e promove o servico para `PAGO` automaticamente quando o saldo zera. |

---

### `RelatorioService`

Consolida dados de multiplos repositorios para gerar relatorios gerenciais.

| Metodo                      | Descricao                                                                                                              |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------|
| `faturamentoPorPeriodo`     | Busca servicos `PAGO` no periodo, monta lista de itens (`cliente`, `descricao`, `data`, `valor`) e calcula total geral. |
| `historicoPorCliente`       | Carrega cliente (404 se ausente), busca seus servicos por data desc e, para cada um, carrega pagamentos e calcula saldo devedor. |
| `listarInadimplentes`       | Agrupa servicos `PENDENTE` e ativos por cliente, calcula saldo de cada um, soma por cliente e ordena por divida desc.  |
| `totaisPorStatus`           | Soma valores e conta servicos ativos filtrados pelo status informado.                                                  |
| `contarInadimplentes`       | Atalho que chama `listarInadimplentes()` e retorna apenas o tamanho da lista.                                         |
| `calcularSaldo` *(privado)* | Subtrai total pago do valor do servico; retorna `0` se negativo.                                                      |

---

### `UsuarioService`

Implementa `UserDetailsService` do Spring Security para autenticacao stateful.

| Metodo               | Descricao                                                                                                                  |
|----------------------|----------------------------------------------------------------------------------------------------------------------------|
| `loadUserByUsername` | Contrato do Spring Security: busca usuario pelo login; lanca `UsernameNotFoundException` se ausente. Invocado automaticamente pelo `AuthenticationManager`. |
| `buscarPorLogin`     | Consulta opcional por login; retorna `Optional.empty()` sem lancar excecao.                                               |
| `getUsuarioLogado`   | Extrai login do `Authentication` ativo e busca no banco; lanca `IllegalStateException` se sem sessao ou `UsernameNotFoundException` se usuario nao existir mais. |

---

### `LogAcaoService`

Registra acoes de escrita para auditoria sem interromper a operacao principal.

| Metodo      | Descricao                                                                                                                                                   |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `registrar` | Recebe `login`, `acao`, `entidade` e `entidadeId`; busca o `Usuario` no banco, cria o `LogAcao` com timestamp e persiste. Se o usuario nao for encontrado ou ocorrer qualquer excecao, loga o erro via SLF4J e retorna silenciosamente — o fluxo principal nunca e interrompido por falha de log. |

**Acoes registradas:**

| Service        | Acao                     |
|----------------|--------------------------|
| ClienteService | `CADASTRAR_CLIENTE`      |
| ClienteService | `ATUALIZAR_CLIENTE`      |
| ClienteService | `REMOVER_CLIENTE`        |
| ClienteService | `RESTAURAR_CLIENTE`      |
| ServicoService | `CADASTRAR_SERVICO`      |
| ServicoService | `ATUALIZAR_SERVICO`      |
| ServicoService | `REMOVER_SERVICO`        |
| ServicoService | `RESTAURAR_SERVICO`      |
| ServicoService | `ALTERAR_STATUS_SERVICO` |

---

## Regras de negocio

### Fluxo de status do servico

```
A_COBRAR --> PENDENTE --> PAGO
```

- Transicoes invalidas (ex: `A_COBRAR` diretamente para `PAGO`, ou retroceder) retornam **409 Conflict**.
- Nenhum pagamento e aceito em servico com status `A_COBRAR` ou `PAGO`.

### Pagamentos parciais

- Aceitos apenas quando o servico esta com status `PENDENTE`.
- O saldo devedor (`valor - soma(valorPago)`) nunca pode ficar negativo.
- Quando o saldo zera, o status do servico e alterado automaticamente para `PAGO`.

### Soft delete

Clientes e servicos nao sao removidos fisicamente. O campo `ativo` e setado para `false`. Registros excluidos aparecem nos endpoints `/excluidos` e podem ser restaurados.

### Controle de acesso por perfil

| Perfil        | Acesso permitido                                                                              |
|---------------|-----------------------------------------------------------------------------------------------|
| `FUNCIONARIO` | `/api/clientes/**`, `/api/servicos/**`, `/api/inadimplentes/**`, `/api/categorias/**`         |
| `ADMIN`       | Todos os anteriores + `/api/relatorios/**`, `/api/logs`                                       |
| Publico       | `/api/auth/login`, `/api/auth/logout`, recursos estaticos                                     |

---

## Respostas de erro

Todos os erros retornam JSON estruturado:

```json
{ "erro": "mensagem descritiva" }
```

| Excecao                    | HTTP |
|----------------------------|------|
| `IllegalArgumentException` | 400  |
| `IllegalStateException`    | 409  |
| `NoSuchElementException`   | 404  |
| `Exception` (generico)     | 500  |

---

## Testes

```bash
mvn test
```

| Suite                       | Testes | Status   |
|-----------------------------|--------|----------|
| ClienteServiceTest          | 5      | Passando |
| ServicoServiceTest          | 5      | Passando |
| PagamentoParcialServiceTest | 4      | Passando |
| RelatorioServiceTest        | 6      | Passando |
| UsuarioServiceTest          | 6      | Passando |
| LogAcaoServiceTest          | 4      | Passando |
| **Total**                   | **30** | **Verde**|

---

## Backup (RNF-004)

O sistema nao implementa backup automatico. O backup e responsabilidade do administrador do PostgreSQL:

```bash
pg_dump -U postgres -d crediario -f backup_crediario_$(date +%Y%m%d).sql
```

Recomenda-se executar diariamente fora do horario de uso.

---

## Decisoes tecnicas notaveis

| Decisao | Motivo |
|---------|--------|
| `categoria_id NOT NULL` no banco | Todo servico contabil tem natureza (IRPF, ITR, etc.). A categoria "Outros" cobre casos livres. |
| `CascadeType.PERSIST` e `MERGE` apenas (sem `ALL`) | Evitar delete em cascata acidental em pagamentos ao excluir servico. |
| `ddl-auto=validate` | Schema e versionado manualmente via `schema.sql`; Hibernate so valida, nao altera. |
| `spring.sql.init.mode=always` + `defer-datasource-initialization=false` | Garante que `schema.sql` e `data.sql` rodam antes da validacao do Hibernate. |
| Soft delete com campo `ativo` | Permite auditoria e restauracao. Nunca apaga dados fisicamente. |
| `UserDetails` na entidade `Usuario` | Abordagem simples adequada para sistema interno pequeno. |
| `SecurityContextHolder` para login no service | Evita alterar assinaturas dos metodos publicos ao integrar audit logging. |
| CSRF desabilitado | API REST com frontend JS; tokens CSRF nao sao necessarios neste cenario. |
| Autenticacao stateful (sessao HTTP) | Sem JWT — adequado para sistema interno sem necessidade de escalabilidade horizontal. |
| Pagamento exige status `PENDENTE` | Servico `A_COBRAR` ainda nao foi cobrado ao cliente — registrar pagamento neste estado seria inconsistente com o fluxo de negocio. |
| `LogAcaoService` com try/catch isolado | Falha de auditoria nao pode interromper a operacao principal do negocio. |
