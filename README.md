<p align="center">
  <img src="https://i.ibb.co/mVnvPCSq/imagem-2026-06-09-161619372.png" alt="Teamlancer" width="160"/>
</p>

<h1 align="center">Teamlancer</h1>

<p align="center">
  <strong>O marketplace onde empresas contratam times, não freelancers avulsos.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-black?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.6-black?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Angular-17+-black?style=flat-square&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-black?style=flat-square&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/status-MVP-black?style=flat-square" />
</p>

---

## O problema

Contratar desenvolvimento de software é lento, caro e arriscado. O modelo atual força empresas a entrevistar, filtrar e montar equipes do zero — toda vez que um novo projeto começa.

| | Freelancers avulsos | Teamlancer |
|---|---|---|
| Tempo até iniciar o projeto | ~17 dias | ~2,5 dias |
| Risco de abandono | Alto (~68%) | Baixo (~18%) |
| Custo de processo seletivo | R$ 4–6k | Incluso |
| Entrosamento da equipe | Construído do zero | Pré-existente |
| Previsibilidade de entrega | Baixa | Alta |

## A solução

O **Teamlancer** é um marketplace onde **equipes pré-formadas e verificadas** se cadastram como uma unidade e ficam disponíveis para contratação. O cliente encontra um time completo — com portfólio real, score de reputação e membros com papéis definidos — e contrata em minutos.

```
Cliente → Busca por stack/especialidade → Vê perfil do time → 
Envia proposta → Time aceita → Projeto começa → Chat + acompanhamento → 
Projeto concluído → Avaliação mútua → Score atualizado
```

---

## Funcionalidades

### Para clientes
- Busca de equipes por tecnologia, especialidade ou score
- Perfil completo do time com portfólio e membros
- Envio de proposta de projeto com orçamento e prazo
- Dashboard com status dos projetos em tempo real
- Chat direto com a equipe por projeto
- Avaliação ao final do projeto

### Para equipes
- Cadastro do time com membros e papéis definidos
- Gerenciamento de convites e solicitações de entrada
- Recebimento e gestão de propostas de clientes
- Atualização de status do projeto (Em andamento → Em teste → Concluído)
- Score de reputação calculado por avaliações recebidas

### Plataforma
- Autenticação JWT com refresh token
- Sistema de log de auditoria
- Documentação automática via Swagger UI
- Modo claro e escuro

---

## Stack técnica

### Backend
```
Java 21 + Spring Boot 4.0.6
├── Spring Security + JWT (autenticação stateless)
├── Spring Data JPA + Hibernate 6
├── Flyway (migrations versionadas)
├── PostgreSQL 15 (ENUMs nativos, arrays, JSONB)
├── MapStruct (mapeamento entity ↔ DTO em compile time)
└── Springdoc OpenAPI (Swagger UI automático)
```

### Frontend
```
Angular 17+ (Standalone Components + Signals)
├── Lazy loading por feature
├── JWT Interceptor com refresh automático
├── Guards de rota (authGuard / guestGuard)
├── Tema claro/escuro via CSS custom properties
└── Phosphor Icons + Geist font
```

### Banco de dados
```
PostgreSQL
├── ENUMs nativos: user_role, request_status, project_status, rater_type
├── GIN index em tech_stack (array de tecnologias)
├── JSONB em audit_logs (metadados flexíveis)
└── 15 migrations Flyway (V1 → V15)
```

---

## Arquitetura

```
Backend/
└── src/main/java/sharktank/teamlancer/
    ├── config/          # SecurityConfig, SwaggerConfig
    ├── domain/
    │   ├── user/        # entity, repo, service, controller, dto
    │   ├── team/        # entity, repo, service, controller, dto
    │   ├── project/     # entity, repo, service, controller, dto
    │   ├── request/     # entity, repo, service, controller, dto
    │   ├── chat/        # entity, repo, service, controller, dto
    │   └── rating/      # entity, repo, service, controller, dto
    ├── security/        # JwtService, JwtAuthFilter, UserDetailsServiceImpl
    └── shared/
        ├── audit/       # AuditLogEntity, AuditLogRepository
        └── exception/   # GlobalExceptionHandler, BusinessException

Frontend/
└── src/app/
    ├── core/
    │   ├── models/      # interfaces TypeScript alinhadas com o backend
    │   ├── services/    # auth, team, request, project, chat, rating, theme
    │   ├── guards/      # authGuard, guestGuard
    │   └── interceptors/# jwt.interceptor (refresh automático)
    ├── shared/
    │   └── components/  # navbar
    └── features/
        ├── home/        # homepage com featured teams
        ├── teams/       # listagem com busca e paginação
        ├── team-detail/ # perfil completo
        ├── auth/        # login e registro
        ├── dashboard/   # painel do cliente e do membro
        ├── request/     # formulário de proposta + confirmação
        ├── project-detail/ # chat + status + avaliação
        └── my-team/     # gerenciamento da equipe
```

---

## Modelo de dados

```
users ──────────────────────────────────────────────────────
  id · email · name · role (CLIENT|MEMBER|ADMIN) · location

user_credentials ───────────────────────────────────────────
  user_id · password_hash · refresh_token_hash

teams ──────────────────────────────────────────────────────
  id · owner_id · name · slug · description · tech_stack[] · team_score

team_members ───────────────────────────────────────────────
  team_id · user_id · role_in_team

project_requests ───────────────────────────────────────────
  client_id · team_id · project_name · status (PENDING|ACCEPTED|REJECTED|CANCELLED)

projects ───────────────────────────────────────────────────
  request_id · client_id · team_id · status (IN_PROGRESS|IN_TESTING|COMPLETED)

chat_messages ──────────────────────────────────────────────
  project_id · sender_id · content · read_at

ratings ────────────────────────────────────────────────────
  project_id · rater_id · rater_type (CLIENT|TEAM) · score (1-5) · comment

audit_logs ─────────────────────────────────────────────────
  user_id · action · entity_type · entity_id · metadata (JSONB)
```

---

## Como rodar localmente

### Pré-requisitos
- Java 21+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.9+

### Backend

```bash
# 1. Clone o repositório
git clone https://github.com/GuiBSantos/teamlancer.git
cd teamlancer/Backend

# 2. Crie o banco
psql -U postgres -c "CREATE DATABASE teamlancer;"

# 3. Configure as credenciais locais
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties

# Edite o arquivo com seus dados:
# spring.datasource.password=sua_senha
# app.jwt.secret=$(openssl rand -hex 64)

# 4. Suba a aplicação (Flyway cria todas as tabelas automaticamente)
./mvnw spring-boot:run
```

> Swagger disponível em: http://localhost:8080/swagger-ui.html

### Frontend

```bash
cd teamlancer/Frontend
npm install
ng serve
```

> Aplicação em: http://localhost:4200

### Usuários de demonstração

| E-mail | Senha | Papel |
|---|---|---|
| `cliente1@empresa.com` | `Demo@1234` | Cliente |
| `cliente2@empresa.com` | `Demo@1234` | Cliente |
| `alice@teamlancer.dev` | `Demo@1234` | Membro — Frontend Dev |
| `bob@teamlancer.dev` | `Demo@1234` | Membro — Backend Dev |

---

## Endpoints principais

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cadastro |
| `POST` | `/api/auth/login` | Login |
| `GET` | `/api/teams` | Lista equipes (busca por ?q=) |
| `GET` | `/api/teams/featured` | Top 3 para a homepage |
| `GET` | `/api/teams/{slug}` | Perfil completo |
| `POST` | `/api/requests` | Enviar proposta |
| `GET` | `/api/requests/me` | Minhas solicitações |
| `PATCH` | `/api/requests/{id}/status` | Aceitar / rejeitar |
| `GET` | `/api/projects/{id}/chat` | Mensagens do projeto |
| `POST` | `/api/projects/{id}/chat` | Enviar mensagem |
| `PATCH` | `/api/projects/{id}/status` | Avançar status do projeto |
| `POST` | `/api/projects/{id}/ratings` | Avaliar (após conclusão) |

---

## Por que times e não freelancers?

**Eficiência**: Um time pré-formado tem comunicação, processos e divisão de papéis já estabelecidos. Projetos iniciam em dias, não semanas.

**Previsibilidade**: Com histórico real de projetos anteriores e score calculado por avaliações, o cliente sabe o que esperar antes de contratar.

**Accountability**: Todos os membros têm interesse no sucesso do projeto — a reputação do time inteiro está em jogo.

**Custo real**: O processo seletivo de freelancers avulsos tem custo invisível (tempo de RH, ferramentas, entrevistas, onboarding). O Teamlancer elimina esse overhead.

---

<p align="center">
  <sub>Teamlancer — Hire a team, not a freelancer.</sub>
</p>
