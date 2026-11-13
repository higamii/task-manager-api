# 📋 Task Manager API

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)
![Build](https://github.com/higamii/task-manager-api/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue)

API REST para gerenciamento de tarefas, construída com **Java 17** e **Spring Boot**, seguindo arquitetura em camadas, boas práticas de validação, tratamento de erros padronizado e testes automatizados.

## ✨ Funcionalidades

- CRUD completo de tarefas
- Filtro por **status** e **prioridade** com **paginação** e ordenação
- Regras de negócio no domínio:
  - tarefas concluídas (`DONE`) não podem ser editadas nem reabertas
  - o prazo (`dueDate`) não pode estar no passado
- Endpoint de **resumo** com a contagem de tarefas por status
- Validação de entrada com Bean Validation
- Erros padronizados seguindo a **RFC 7807 (Problem Details)**
- Documentação interativa com **Swagger / OpenAPI**
- Testes unitários (Mockito) e de integração (MockMvc)
- Pipeline de CI com **GitHub Actions** e imagem **Docker**

## 🛠️ Tecnologias

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3 (Web, Data JPA, Validation) |
| Banco de dados | H2 (em memória) |
| Documentação | springdoc-openapi |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc |
| Build | Maven |
| DevOps | Docker, GitHub Actions |

## 🏗️ Arquitetura

```
src/main/java/com/higamii/taskmanager
├── controller   # Camada HTTP (endpoints REST)
├── service      # Regras de negócio
├── repository   # Acesso a dados (Spring Data JPA)
├── model        # Entidades e enums de domínio
├── dto          # Objetos de entrada e saída (records)
└── exception    # Exceções e handler global
```

Os DTOs são `records` Java, evitando expor a entidade diretamente na API.

## 🚀 Como executar

**Pré-requisitos:** Java 17+ e Maven 3.9+ (ou Docker).

```bash
git clone https://github.com/higamii/task-manager-api.git
cd task-manager-api
mvn spring-boot:run
```

Com Docker:

```bash
docker build -t task-manager-api .
docker run -p 8080:8080 task-manager-api
```

Depois, acesse a documentação em **http://localhost:8080/swagger-ui.html**.

## 🧪 Testes

```bash
mvn test
```

## 📡 Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/tasks` | Cria uma tarefa |
| `GET` | `/api/tasks?status=&priority=&page=&size=` | Lista com filtros e paginação |
| `GET` | `/api/tasks/{id}` | Busca por id |
| `PUT` | `/api/tasks/{id}` | Atualiza uma tarefa |
| `PATCH` | `/api/tasks/{id}/status` | Altera o status |
| `DELETE` | `/api/tasks/{id}` | Remove uma tarefa |
| `GET` | `/api/tasks/summary` | Resumo por status |

### Exemplo

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Estudar Spring Boot", "description": "Revisar JPA", "priority": "HIGH", "dueDate": "2030-12-31"}'
```

```json
{
  "id": 1,
  "title": "Estudar Spring Boot",
  "description": "Revisar JPA",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2030-12-31",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00"
}
```

Exemplo de erro de validação (`400`):

```json
{
  "title": "Erro de validação",
  "status": 400,
  "detail": "Dados inválidos na requisição",
  "errors": { "title": "O título é obrigatório" }
}
```

## 🔮 Próximos passos

- [ ] Autenticação com Spring Security + JWT
- [ ] Persistência com PostgreSQL e migrations com Flyway
- [ ] Associação de tarefas a usuários
- [ ] Cobertura de testes com JaCoCo

## 👤 Autor

**Thiago Carvalho**
GitHub: [@higamii](https://github.com/higamii)

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
