# 📚 ForumHub API

<p align="center">
  <img src="assets/Badge-Spring.png" width="220"/>
</p>

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.11-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-green)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D)
![License](https://img.shields.io/badge/License-Educational-lightgrey)

API REST para gerenciamento de um fórum educacional, desenvolvida com **Spring Boot 3** e autenticação via **JWT**.

---

## 🚀 Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.11 |
| Spring Security | 6.x |
| Spring Data JPA | 3.x |
| MySQL | 8.x |
| Flyway | — |
| Auth0 Java JWT | 4.5.0 |
| Lombok | — |
| SpringDoc OpenAPI (Swagger) | 2.8.15 |
| H2 (testes) | — |

---

## 📐 Arquitetura

```
src/main/java/br/com/alura/forumhub/
├── config/           # Configurações gerais da aplicação (SpringDoc)
├── controllers/      # Endpoints REST (Autenticacao, Topico, Curso, Resposta, Usuario)
├── dto/              # Records de entrada e saída da API
│   ├── curso/
│   ├── resposta/
│   ├── security/
│   ├── topico/
│   └── usuario/
├── exception/        # Tratamento global de exceções
├── model/            # Entidades JPA (Topico, Usuario, Perfil, Curso, Resposta)
├── repository/       # Interfaces Spring Data JPA
├── security/         # Filtro JWT, TokenService, SecurityConfiguration
└── service/          # Regras de negócio e validações
    └── validation/   # Validadores personalizados por entidade e operação
        ├── comum/    # Validadores compartilhados (ex.: ValidadorAutorExiste)
        ├── curso/
        ├── resposta/
        ├── topico/
        └── usuario/
```

---

## 🗂️ Modelo de Dados

```
Perfil  <──── usuarios_perfis ────> Usuario (ativo)
                                       │
                        Curso  ─────  Topico  ───── StatusTopico (enum)    
                                       │
                                     Resposta
```

As migrações do banco são gerenciadas pelo **Flyway** (diretório `src/main/resources/db/migration`).

### 🟢 Soft Delete

Usuários não são removidos fisicamente do banco. O campo `ativo` controla o estado da conta:

- `true` → usuário ativo
- `false` → usuário desativado

Usuários desativados:

- Não conseguem autenticar
- Não aparecem na listagem padrão (`GET /usuario`)
- Podem ser reativados futuramente

---

## 🔐 Segurança

- Autenticação **stateless** via **JWT (Bearer Token)**
- Senhas armazenadas com hash **BCrypt**
- Autorização baseada em roles com `@PreAuthorize` (`@EnableMethodSecurity`)
- Endpoints públicos:
    - `POST /login`
    - `POST /usuario` (cadastro de novo usuário)
    - Documentação Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)
- Todos os demais endpoints exigem token válido no header `Authorization`
- Endpoint `GET /usuario/listar-todos` exige role **ADMIN**

---

## 📡 Endpoints

### Autenticação

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/login` | Autentica o usuário e retorna o token JWT | ❌ |

**Body de exemplo:**
```json
{
  "email": "usuario@email.com",
  "senha": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Tópicos

> Todos os endpoints abaixo requerem `Authorization: Bearer <token>`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/topicos` | Cadastra um novo tópico |
| `GET` | `/topicos` | Lista tópicos com paginação (filtros: `curso`, `ano`) |
| `GET` | `/topicos/{id}` | Detalha um tópico específico |
| `PUT` | `/topicos/{id}` | Atualiza um tópico existente |
| `DELETE` | `/topicos/{id}` | Remove um tópico |

**Listagem — parâmetros de query:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `curso` | `String` | Não | Filtra pelo nome do curso |
| `ano` | `Integer` | Não | Filtra pelo ano de criação |
| `page` | `Integer` | Não | Número da página (padrão: 0) |
| `size` | `Integer` | Não | Itens por página (padrão: 10) |
| `sort` | `String` | Não | Campo de ordenação (padrão: `dataCriacao,asc`) |

---

### Cursos

> Todos os endpoints abaixo requerem `Authorization: Bearer <token>`

| Método   | Endpoint       | Descrição                                          |
|----------|----------------|----------------------------------------------------|
| `POST`   | `/cursos`      | Cadastra um novo curso                             |
| `GET`    | `/cursos`      | Lista cursos com paginação (ordenação: `nome,asc`) |
| `GET`    | `/cursos/{id}` | Detalha um curso específico                        |
| `PUT`    | `/cursos/{id}` | Atualiza um curso existente                        |
| `DELETE` | `/cursos/{id}` | Remove um curso                                    |

---

### Respostas

> Todos os endpoints abaixo requerem `Authorization: Bearer <token>`

| Método   | Endpoint          | Descrição                                                    |
|----------|-------------------|--------------------------------------------------------------|
| `POST`   | `/respostas`      | Cadastra uma nova resposta em um tópico                      |
| `GET`    | `/respostas`      | Lista respostas com paginação (ordenação: `dataCriacao,asc`) |
| `GET`    | `/respostas/{id}` | Detalha uma resposta específica                              |
| `PUT`    | `/respostas/{id}` | Atualiza uma resposta existente                              |
| `DELETE` | `/respostas/{id}` | Remove uma resposta                                          |

---

### Usuários

| Método   | Endpoint                | Descrição                                       | Auth    |
|----------|-------------------------|-------------------------------------------------|---------|
| `POST`   | `/usuario`              | Cadastra um novo usuário                        | ❌       |
| `GET`    | `/usuario`              | Lista usuários ativos com paginação             | ✅       |
| `GET`    | `/usuario/listar-todos` | Lista todos os usuários (requer role **ADMIN**) | ✅ ADMIN |
| `GET`    | `/usuario/{id}`         | Detalha um usuário específico                   | ✅       |
| `PUT`    | `/usuario/{id}`         | Atualiza um usuário existente                   | ✅       |
| `DELETE` | `/usuario/{id}`         | Remove um usuário                               | ✅       |

---

## ⚙️ Configuração e Variáveis de Ambiente

Configure as seguintes variáveis de ambiente antes de iniciar a aplicação:

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `FORUMHUB_DB_URL` | URL de conexão JDBC com o MySQL | `jdbc:mysql://localhost:3306/forumhub` |
| `MYSQL_DB_USER` | Usuário do banco de dados | `root` |
| `MYSQL_DB_PASS` | Senha do banco de dados | `senha` |
| `JWT_SECRET` | Chave secreta para assinar os tokens JWT | `minha-chave-secreta-super-segura` |
| `JWT_EXPIRATION` | Tempo de expiração do token em ms (padrão: 86400000 = 24h) | `86400000` |

> ⚠️ **Nunca commite o valor real de `JWT_SECRET` no repositório.** Use variáveis de ambiente, um arquivo `.env` (
> adicionado ao `.gitignore`) ou um gerenciador de segredos.

---

## 📌 Status Codes

| Código | Quando ocorre                                   |
|--------|-------------------------------------------------|
| `200`  | Requisição bem-sucedida                         |
| `201`  | Recurso criado com sucesso                      |
| `400`  | Violação de regra de negócio ou dados inválidos |
| `401`  | Não autenticado (token ausente ou inválido)     |
| `403`  | Acesso negado (role insuficiente)               |
| `404`  | Recurso não encontrado                          |

---

## 🏃 Como executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/forumhub.git
   cd forumhub
   ```

2. **Crie o banco de dados no MySQL:**
   ```sql
   CREATE DATABASE forumhub;
   ```

3. **Configure as variáveis de ambiente** (veja tabela acima).

4. **Execute a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Ou gere o `.jar` e execute:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/forumhub-1.0.0.jar
   ```

5. A API estará disponível em `http://localhost:8080`.

---

## 📖 Documentação Interativa (Swagger)

Após iniciar a aplicação, acesse:

```
http://localhost:8080/swagger-ui.html
```

A documentação OpenAPI em formato JSON está disponível em:

```
http://localhost:8080/v3/api-docs
```

---

## 🧪 Testes

O projeto utiliza **JUnit 5**, **Spring Boot Test**, **Spring Security Test** e banco **H2 em memória** para os testes.

```bash
./mvnw test
```

Os relatórios são gerados em `target/surefire-reports/`.

### Cobertura de testes

**Controllers (integração):**

- `TopicoControllerTest`
- `CursoControllerTest`
- `RespostaControllerTest`
- `UsuarioControllerTest`

**Services (unitários):**

- `TopicoServiceTest`
- `CursoServiceTest`
- `RespostaServiceTest`
- `UsuarioServiceTest`

**Repository:**

- `TopicoRepositoryTest`

**Security:**

- `TokenServiceTest`
- `AutorizacaoServiceTest`
- `SecurityFilterTest`

**Validadores — Tópico:**

- `ValidadorTopicoDuplicadoTest` — duplicidade no cadastro
- `ValidadorAutorExisteTest` — existência do autor
- `ValidadorCursoExisteTest` — existência do curso
- `ValidadorAutorTopicoTest` — autor na atualização
- `ValidadorDuplicidadeTopicoAoAtualizarTest` — duplicidade na atualização
- `ValidadorAutorExcluirTopicoTest` — autor na exclusão

**Validadores — Resposta:**

- `ValidadorTopicoExisteTest` — existência do tópico ao cadastrar
- `ValidadorTopicoFechadoTest` — tópico fechado ao cadastrar
- `ValidadorTopicoSolucionadoTest` — tópico solucionado ao cadastrar
- `ValidadorAutorDaRespostaTest` — autor na atualização
- `ValidadorAutorExcluirRespostaTest` — autor na exclusão

**Validadores — Curso:**

- `ValidadorCursoDuplicadoTest` — duplicidade no cadastro
- `ValidadorCursoDuplicadoAtualizacaoTest` — duplicidade na atualização

**Validadores — Usuário:**

- `ValidadorEmailDuplicadoTest` — e-mail duplicado no cadastro
- `ValidadorEmailDuplicadoAtualizacaoTest` — e-mail duplicado na atualização

---

## 📁 Estrutura de Migrações

| Arquivo                                          | Descrição                                      |
|--------------------------------------------------|------------------------------------------------|
| `V1__create-table-perfis.sql`                    | Cria a tabela de perfis                        |
| `V2__create-table-usuarios.sql`                  | Cria a tabela de usuários                      |
| `V3__create-table-usuarios_perfis.sql`           | Cria a tabela de relacionamento usuário-perfil |
| `V4__create_table_cursos.sql`                    | Cria a tabela de cursos                        |
| `V5__create_table_topicos.sql`                   | Cria a tabela de tópicos                       |
| `V6__create_table_respostas.sql`                 | Cria a tabela de respostas                     |
| `V7__alter_unique_constraint_usuarios_email.sql` | Adiciona constraint única no e-mail do usuário |
| `V8__insert_perfis.sql`                          | Insere os perfis padrão                        |
| `V9__add_column_ativo_usuarios.sql`              | Adiciona coluna `ativo` na tabela de usuários  |

---

## 👥 Autor

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/PxS00">
        <img src="https://github.com/PxS00.png" width="100px;" alt="Lucas Rossoni"/><br>
        <sub><b>Lucas Rossoni Dieder</b></sub>
      </a><br>
      <a href="https://www.linkedin.com/in/lucas-rossoni-dieder-32242a353/">LinkedIn</a>
    </td>
  </tr>
</table>

---

## 📄 Licença

Este projeto foi desenvolvido como parte de um desafio da **Alura** e é de uso educacional.
