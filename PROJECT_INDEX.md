# Project Index: Qcheck

Generated: 2026-05-17

## 📁 Project Structure

```
Qcheck/
├── back/                          # Spring Boot backend (Java 17)
│   ├── pom.xml
│   └── src/main/java/com/qcheck/qcheck/
│       ├── QcheckApplication.java          # Entry point
│       ├── controller/CompanyController.java
│       ├── entity/Company.java
│       ├── entity/CompanyStatus.java       # Enum: ACTIVE, INACTIVE, SUSPENDED
│       ├── repository/CompanyRepository.java
│       └── service/DatabaseConnectionService.java
│   └── src/main/resources/
│       ├── application.properties          # DB config (PostgreSQL :5432/qcheck)
│       └── database_setup.sql              # Tables: company, emp, company_emp_hist
├── front/                         # Angular 20 frontend (SSR)
│   └── src/app/
│       ├── app.ts                          # Root component
│       ├── app.routes.ts                   # Routes: / → /login, /login
│       ├── login.component.ts              # Login page (email/password form)
│       ├── app.config.ts                   # Angular providers
│       └── app.config.server.ts            # SSR config
├── image/                         # QCheck_example.png, login.png
└── README.md
```

## 🚀 Entry Points

- **Backend**: `back/src/main/java/com/qcheck/qcheck/QcheckApplication.java` — Spring Boot main class, port **8081**
- **Frontend**: `front/src/main.ts` — Angular bootstrap (default port **4200**)
- **Frontend SSR**: `front/src/server.ts` — Express SSR server
- **DB Setup**: `back/src/main/resources/database_setup.sql` — Run once to init PostgreSQL

## 📦 Core Modules

### Backend (Spring Boot 3.4.10 / Java 17)

| File | Purpose |
|------|---------|
| `controller/CompanyController.java` | REST endpoints: `GET /api/companies/*` (all, by id, by status, search, count, sorted) |
| `entity/Company.java` | JPA entity → `company` table: `id`, `name`, `status` |
| `entity/CompanyStatus.java` | Enum: `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `repository/CompanyRepository.java` | JpaRepository + custom queries (findByStatus, findByNameContaining, countByStatus, etc.) |
| `service/DatabaseConnectionService.java` | Runs DB health check on `ApplicationReadyEvent` startup |

### Frontend (Angular 20 Standalone / SSR)

| File | Purpose |
|------|---------|
| `app/app.ts` | Root component — renders `<router-outlet>` |
| `app/app.routes.ts` | Route table: `''` → redirect `/login`; `/login` lazy-loads `LoginComponent` |
| `app/login.component.ts` | Reactive form login page (email + password validators, signal-based state) |
| `app/app.config.ts` | Angular providers: `provideRouter`, `provideClientHydration`, `provideHttpClient` |

## 🗄️ Database Schema

**Engine**: PostgreSQL 17 · DB name: `qcheck` · Default user: `postgres`

| Table | Key Columns | Notes |
|-------|-------------|-------|
| `company` | `company_seq (PK)`, `name`, `status`, `created_at`, `created_ep` | Active companies managed by API |
| `emp` | `emp_seq (PK)`, `emp_id (UNIQUE)`, `emp_pw`, `emp_nm`, `use_yn` | Employees/users (bcrypt pw expected) |
| `company_emp_hist` | `hist_seq (PK)`, `company_seq (FK)`, `emp_seq (FK)`, `start_date` | Company-employee membership history (not yet active) |

## 🔧 Configuration

| File | Key Settings |
|------|-------------|
| `back/src/main/resources/application.properties` | Server port `8081`; PostgreSQL `localhost:5432/qcheck`; JPA `ddl-auto=update`, `show-sql=true` |
| `front/package.json` | Angular CLI 20.3.2; `ng serve` (dev), `ng build` (prod), `serve:ssr:front` (SSR) |
| `front/tsconfig.json` | TypeScript strict config for Angular |
| `front/angular.json` | Angular workspace build config |

## 📚 REST API Surface

Base URL: `http://localhost:8081/api/companies`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/all` | All companies |
| GET | `/{id}` | Company by ID |
| GET | `/status/{status}` | Filter by status |
| GET | `/active` | Active companies (custom JPQL) |
| GET | `/search?keyword=` | Name contains search |
| GET | `/search-ignore-case?name=` | Case-insensitive search |
| GET | `/name?exact=` | Exact name match |
| GET | `/exists?name=` | Existence check |
| GET | `/count` | Total count |
| GET | `/count/status/{status}` | Count by status |
| GET | `/sorted` | All, sorted by name ASC |
| GET | `/sorted/status/{status}` | Status-filtered, sorted |
| GET | `/help` | API guide (text) |

## 🧪 Test Coverage

| Layer | File | Framework |
|-------|------|-----------|
| Backend | `back/src/test/java/.../QcheckApplicationTests.java` | Spring Boot Test (JUnit 5) |
| Frontend | `front/src/app/app.spec.ts` | Jasmine + Karma |

## 🔗 Key Dependencies

**Backend**
- `spring-boot-starter-web` 3.4.10 — REST API
- `spring-boot-starter-data-jpa` — ORM / repository
- `spring-boot-starter-validation` — Bean validation
- `spring-boot-starter-actuator` — Health/metrics endpoints
- `postgresql` — PostgreSQL JDBC driver
- `lombok` — Boilerplate reduction

**Frontend**
- `@angular/core` ^20.3.0 — Framework
- `@angular/ssr` ^20.3.2 — Server-side rendering
- `@angular/forms` — Reactive forms
- `express` ^5.1.0 — SSR server
- `rxjs` ~7.8.0 — Reactive streams

## 📝 Quick Start

### Backend
```bash
cd back
# Edit src/main/resources/application.properties with your DB credentials
./mvnw spring-boot:run
# Runs on http://localhost:8081
```

### Database
```bash
psql -U postgres
# CREATE DATABASE qcheck;
# \c qcheck
# \i back/src/main/resources/database_setup.sql
```

### Frontend
```bash
cd front
npm install
npm start          # dev server → http://localhost:4200
npm run build      # production build
npm run serve:ssr:front  # SSR server
```

## 🏗️ Architecture Notes

- **Monorepo layout**: `back/` (Spring Boot) + `front/` (Angular) are independent projects
- **No auth yet**: `LoginComponent` has TODO for actual API call; currently simulates 1s delay
- **DB-JPA column mismatch**: `company` table PK is `company_seq` in SQL but JPA entity maps to `id` — requires verification
- **`company_emp_hist`**: table exists in SQL but not mapped to JPA entity yet (marked "not used" in SQL comments)
- **SSR**: Angular app configured for server-side rendering via `@angular/ssr` + Express
