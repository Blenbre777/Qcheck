# Qcheck 시스템 설계 문서

> 작성일: 2026-05-17  
> 기준: 기존 코드 + UI 목업(image/QCheck_example.png) + DB 스키마

---

## 0. 프로젝트 개요

**Qcheck**는 제조 현장에서 발생하는 **불량(Defect)을 라인·부품·불량코드 단위로 기록·집계**하는 멀티테넌트 품질 SaaS다.

### 화면 구성 (목업 기준)

| 화면 | 경로 | 설명 |
|------|------|------|
| 로그인 | `/login` | 이메일 + 비밀번호 |
| 대시보드 | `/dashboard` | 기간별 불량 현황, Top3 라인/코드, 일별 바 차트 |
| 불량 등록 | `/defects/add` | 라인·부품·불량코드·수량·위치·첨부파일 입력 |
| 불량 목록 | `/defects` | 날짜 범위 검색, 편집/삭제 |

---

## 1. 기존 코드 이슈 (설계 전 선결 사항)

### 1-1. `company` 테이블 PK 불일치 ⚠️
- **SQL**: PK 컬럼명 `company_seq`
- **JPA** (`Company.java`): `@Column(name = "id")`
- **조치**: JPA 컬럼명을 `company_seq`로 수정

```java
// 수정 전
@Column(name = "id")
private Long id;

// 수정 후
@Column(name = "company_seq")
private Long companySeq;
```

### 1-2. 로그인 방식 불일치 ⚠️
- **LoginComponent**: `email` + `password` 입력
- **emp 테이블**: `emp_id` (VARCHAR) + `emp_pw`
- **결정**: `emp` 테이블에 `emp_email` 컬럼 추가, 로그인 식별자를 이메일로 통일

### 1-3. `company_emp_hist` 활성화 여부
- SQL에 정의되어 있으나 JPA 엔티티 미생성, SQL 주석에도 "미사용"으로 표시
- **결정**: 현재 스코프에서는 "사원은 하나의 회사에 소속" 으로 단순화, `emp.company_seq` FK 컬럼으로 대체. `company_emp_hist`는 향후 이직/이동 이력 추적 시 활성화.

### 1-4. 불량 목록의 "Unit" 컬럼 ⚠️
- 목업 불량 목록에 `Unit` 컬럼(P1, P2 값)이 존재하나 불량 등록 폼에는 해당 입력 필드 없음
- **결정**: `unit`을 **생산 공정 단위(공정/라인 내 세부 구분)** 로 해석하고 불량 등록 폼에 추가. 추후 팀 확인 필요.

---

## 2. 도메인 모델

```
Company (1) ──── (N) Emp
    │
    ├── (1:N) Line       (생산 라인)
    ├── (1:N) Part       (부품/제품)
    ├── (1:N) Unit       (공정 단위)
    └── (1:N) DefectCode (불량 코드 마스터)
                 │
                 └── (1:N) Defect (불량 기록)
                              ├── FK: company_seq
                              ├── FK: line_seq
                              ├── FK: part_seq
                              ├── FK: unit_seq
                              ├── FK: defect_code_seq
                              └── FK: emp_seq (등록자)
```

---

## 3. DB 스키마

> 기존 audit 패턴(`created_at / created_ep / updated_at / updated_ep`) 전체 적용.

### 3-1. `company` (기존 — PK 컬럼명 유지)

```sql
company_seq  BIGSERIAL PRIMARY KEY
name         VARCHAR(100) NOT NULL
status       VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED'))
created_at   TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at   TIMESTAMPTZ  DEFAULT NULL
updated_ep   VARCHAR(50)  DEFAULT NULL
```

### 3-2. `emp` (기존 + 신규 컬럼)

```sql
emp_seq      BIGSERIAL PRIMARY KEY
company_seq  BIGINT       NOT NULL REFERENCES company(company_seq)  -- 소속 회사
emp_id       VARCHAR(50)  NOT NULL UNIQUE          -- 로그인 ID
emp_email    VARCHAR(255) NOT NULL UNIQUE          -- 로그인 이메일 (신규)
emp_pw       VARCHAR(255) NOT NULL                  -- bcrypt 해시
emp_nm       VARCHAR(100) NOT NULL
emp_role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
             CHECK (emp_role IN ('SUPER_ADMIN','COMPANY_ADMIN','USER'))
use_yn       VARCHAR(2)   NOT NULL DEFAULT 'Y'
created_at   TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at   TIMESTAMPTZ  DEFAULT NULL
updated_ep   VARCHAR(50)  DEFAULT NULL
```

### 3-3. `line` (신규)

```sql
line_seq     BIGSERIAL PRIMARY KEY
company_seq  BIGINT       NOT NULL REFERENCES company(company_seq)
line_nm      VARCHAR(100) NOT NULL
use_yn       VARCHAR(2)   NOT NULL DEFAULT 'Y'
created_at   TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at   TIMESTAMPTZ  DEFAULT NULL
updated_ep   VARCHAR(50)  DEFAULT NULL

UNIQUE (company_seq, line_nm)
```

### 3-4. `part` (신규)

```sql
part_seq     BIGSERIAL PRIMARY KEY
company_seq  BIGINT       NOT NULL REFERENCES company(company_seq)
part_nm      VARCHAR(100) NOT NULL
use_yn       VARCHAR(2)   NOT NULL DEFAULT 'Y'
created_at   TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at   TIMESTAMPTZ  DEFAULT NULL
updated_ep   VARCHAR(50)  DEFAULT NULL

UNIQUE (company_seq, part_nm)
```

### 3-5. `unit` (신규 — 공정 단위)

```sql
unit_seq     BIGSERIAL PRIMARY KEY
company_seq  BIGINT       NOT NULL REFERENCES company(company_seq)
unit_nm      VARCHAR(100) NOT NULL   -- 예: P1, P2
use_yn       VARCHAR(2)   NOT NULL DEFAULT 'Y'
created_at   TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at   TIMESTAMPTZ  DEFAULT NULL
updated_ep   VARCHAR(50)  DEFAULT NULL

UNIQUE (company_seq, unit_nm)
```

### 3-6. `defect_code` (신규 — 불량 코드 마스터)

```sql
defect_code_seq  BIGSERIAL PRIMARY KEY
company_seq      BIGINT      NOT NULL REFERENCES company(company_seq)
code_nm          VARCHAR(50) NOT NULL    -- 예: DEFNO1
description      VARCHAR(255)
use_yn           VARCHAR(2)  NOT NULL DEFAULT 'Y'
created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
created_ep       VARCHAR(50) NOT NULL DEFAULT 'SYSTEM'
updated_at       TIMESTAMPTZ DEFAULT NULL
updated_ep       VARCHAR(50) DEFAULT NULL

UNIQUE (company_seq, code_nm)
```

### 3-7. `defect` (신규 — 핵심 트랜잭션 테이블)

```sql
defect_seq       BIGSERIAL PRIMARY KEY
company_seq      BIGINT       NOT NULL REFERENCES company(company_seq)
line_seq         BIGINT       NOT NULL REFERENCES line(line_seq)
part_seq         BIGINT       NOT NULL REFERENCES part(part_seq)
unit_seq         BIGINT       NOT NULL REFERENCES unit(unit_seq)
defect_code_seq  BIGINT       NOT NULL REFERENCES defect_code(defect_code_seq)
quantity         INT          NOT NULL DEFAULT 1 CHECK (quantity > 0)
position         VARCHAR(100)            -- 불량 발생 위치
remarks          TEXT                    -- 비고
occur_date       DATE         NOT NULL   -- 불량 발생일 (created_at과 별도)
image_path       VARCHAR(500)            -- 첨부 이미지 경로
created_at       TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP
created_ep       VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
updated_at       TIMESTAMPTZ  DEFAULT NULL
updated_ep       VARCHAR(50)  DEFAULT NULL
```

### 인덱스

```sql
-- 회사별 날짜 범위 조회 (목록 필터링)
CREATE INDEX idx_defect_company_date ON defect(company_seq, occur_date DESC);

-- 대시보드 집계 (라인별)
CREATE INDEX idx_defect_line ON defect(company_seq, line_seq, occur_date);

-- 대시보드 집계 (코드별)
CREATE INDEX idx_defect_code ON defect(company_seq, defect_code_seq, occur_date);
```

---

## 4. API 설계

> Base URL: `http://localhost:8081/api`  
> 인증: JWT Bearer Token (모든 엔드포인트 필수, `/auth/**` 제외)  
> 멀티테넌트: JWT 클레임의 `company_seq`로 데이터 격리

### 4-1. 인증 (`/api/auth`)

| Method | Path | 설명 | Request | Response |
|--------|------|------|---------|---------|
| POST | `/auth/login` | 로그인 | `{email, password}` | `{accessToken, refreshToken, empInfo}` |
| POST | `/auth/logout` | 로그아웃 | — | 200 OK |
| POST | `/auth/refresh` | 토큰 갱신 | `{refreshToken}` | `{accessToken}` |

**JWT Payload**
```json
{
  "sub": "emp_seq",
  "company_seq": 1,
  "emp_role": "USER",
  "exp": 1234567890
}
```

### 4-2. 대시보드 (`/api/dashboard`)

| Method | Path | 설명 | Query Params |
|--------|------|------|------|
| GET | `/dashboard/summary` | 기간별 전체 요약 | `period=7d\|30d\|custom&from=&to=` |
| GET | `/dashboard/top-lines` | Top 3 라인별 불량 수 | `period=` |
| GET | `/dashboard/top-codes` | Top 3 불량코드별 수 | `period=` |
| GET | `/dashboard/daily` | 일별 불량 건수 (바 차트) | `from=&to=` |

**GET `/dashboard/summary` 응답**
```json
{
  "totalDefects": 24,
  "totalPositions": 5,
  "period": { "from": "2025-09-01", "to": "2025-09-07" }
}
```

**GET `/dashboard/daily` 응답**
```json
[
  { "date": "2025-09-01", "count": 3 },
  { "date": "2025-09-02", "count": 7 }
]
```

### 4-3. 불량 (`/api/defects`)

| Method | Path | 설명 | Request/Query |
|--------|------|------|------|
| GET | `/defects` | 목록 조회 | `from=&to=&keyword=&page=&size=` |
| POST | `/defects` | 불량 등록 | multipart/form-data |
| PUT | `/defects/{defect_seq}` | 불량 수정 | multipart/form-data |
| DELETE | `/defects/{defect_seq}` | 불량 삭제 | — |
| GET | `/defects/{defect_seq}/image` | 첨부 이미지 다운로드 | — |

**POST `/defects` Request (multipart/form-data)**
```
line_seq       : Long
part_seq       : Long
unit_seq       : Long
defect_code_seq: Long
quantity       : Int
position       : String
remarks        : String (optional)
occur_date     : LocalDate (yyyy-MM-dd)
image          : MultipartFile (optional)
```

**GET `/defects` 응답**
```json
{
  "content": [
    {
      "defect_seq": 1,
      "occur_date": "2025-09-11",
      "unit_nm": "P1",
      "line_nm": "L2",
      "part_nm": "PartA",
      "code_nm": "DEFNO1",
      "position": "1",
      "quantity": 3,
      "has_image": true
    }
  ],
  "page": 0,
  "size": 10,
  "total_elements": 8,
  "total_pages": 1
}
```

### 4-4. 마스터 데이터 (`/api/master`)

> 모두 동일한 CRUD 패턴. 드롭다운 용도이므로 목록 조회가 핵심.

| Method | Path | 설명 |
|--------|------|------|
| GET | `/master/lines` | 라인 목록 (use_yn=Y) |
| POST | `/master/lines` | 라인 등록 |
| PUT | `/master/lines/{seq}` | 라인 수정 |
| DELETE | `/master/lines/{seq}` | 라인 삭제(soft: use_yn=N) |
| GET | `/master/parts` | 부품 목록 |
| POST/PUT/DELETE | `/master/parts/{seq}` | 부품 CRUD |
| GET | `/master/units` | 공정 단위 목록 |
| POST/PUT/DELETE | `/master/units/{seq}` | 공정 단위 CRUD |
| GET | `/master/defect-codes` | 불량 코드 목록 |
| POST/PUT/DELETE | `/master/defect-codes/{seq}` | 불량 코드 CRUD |

### 4-5. 파일 업로드 정책

- **저장 방식**: 서버 로컬 파일시스템 (`/files/defects/{company_seq}/{yyyy-MM}/`)
- **허용 확장자**: jpg, jpeg, png, gif, bmp
- **최대 크기**: 10MB
- **응답**: 저장 경로를 `image_path`에 저장, 다운로드는 `/api/defects/{seq}/image`로 제공
- **향후**: AWS S3 또는 외부 오브젝트 스토리지로 교체 예정

---

## 5. 백엔드 패키지 구조

```
com.qcheck.qcheck
├── config/
│   ├── SecurityConfig.java      # Spring Security + JWT 필터
│   ├── JwtConfig.java
│   └── WebMvcConfig.java        # CORS, 파일 서빙
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── DefectController.java
│   └── master/
│       ├── LineController.java
│       ├── PartController.java
│       ├── UnitController.java
│       └── DefectCodeController.java
├── service/
│   ├── AuthService.java
│   ├── DashboardService.java
│   ├── DefectService.java
│   ├── FileStorageService.java
│   └── master/
│       └── (Line|Part|Unit|DefectCode)Service.java
├── repository/
│   ├── EmpRepository.java
│   ├── DefectRepository.java
│   └── master/
│       └── (Line|Part|Unit|DefectCode)Repository.java
├── entity/
│   ├── Company.java       ← company_seq 컬럼명 수정 필요
│   ├── Emp.java
│   ├── Line.java
│   ├── Part.java
│   ├── Unit.java
│   ├── DefectCode.java
│   └── Defect.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── DefectCreateRequest.java
│   │   └── DefectUpdateRequest.java
│   └── response/
│       ├── LoginResponse.java
│       ├── DefectResponse.java
│       ├── DashboardSummaryResponse.java
│       └── DailyStat.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── (BusinessException, ResourceNotFoundException 등)
```

---

## 6. 프론트엔드 라우트 구조

```
/login                      → LoginComponent        (인증 불필요)
/dashboard                  → DashboardComponent    (기본 진입점)
/defects                    → DefectListComponent
/defects/add                → DefectFormComponent
/defects/:defect_seq/edit   → DefectFormComponent
/master/lines               → LineManageComponent   (마스터 관리)
/master/parts               → PartManageComponent
/master/units               → UnitManageComponent
/master/defect-codes        → DefectCodeManageComponent
```

**컴포넌트별 주요 의존성**

| 컴포넌트 | 사용 API | 상태 관리 |
|---------|---------|---------|
| LoginComponent | POST /auth/login | Signal (isLoading, errorMessage) |
| DashboardComponent | GET /dashboard/summary, /top-lines, /top-codes, /daily | Signal |
| DefectListComponent | GET /defects | Signal (pagination, filter) |
| DefectFormComponent | GET /master/*, POST|PUT /defects | ReactiveForm + Signal |

**인증 가드**: `AuthGuard` — JWT 없으면 `/login`으로 리다이렉트  
**HTTP Interceptor**: `JwtInterceptor` — 모든 요청 헤더에 `Authorization: Bearer {token}` 추가

---

## 7. 보안 설계

| 항목 | 결정 |
|------|------|
| 인증 방식 | JWT (Access Token 30분, Refresh Token 7일) |
| 비밀번호 해시 | BCrypt (Spring Security 기본) |
| 멀티테넌트 격리 | 모든 쿼리에 `company_seq` 조건 강제 (서비스 레이어) |
| CORS | `http://localhost:4200` 허용 (개발), 환경변수로 설정 |
| Refresh Token 저장 | DB 테이블 `emp_token` 또는 Redis (현재는 DB) |
| 역할(Role) | `SUPER_ADMIN` (전체), `COMPANY_ADMIN` (회사 관리), `USER` (불량 등록/조회) |

---

## 8. 구현 순서 (권장)

```
Phase 1 — 기반
  1. Company JPA 컬럼명 수정 (company_seq)
  2. emp 테이블 emp_email / company_seq / emp_role 컬럼 추가
  3. JWT 인증 구현 (AuthController, JwtTokenProvider, SecurityConfig)
  4. AuthGuard + JwtInterceptor (프론트)

Phase 2 — 마스터 데이터
  5. Line / Part / Unit / DefectCode 엔티티·리포지토리·서비스·컨트롤러
  6. 마스터 관리 화면 4개 (프론트)

Phase 3 — 핵심 기능
  7. Defect 엔티티·리포지토리·서비스·컨트롤러
  8. 파일 업로드 (FileStorageService)
  9. 불량 목록 / 등록 / 수정 / 삭제 (프론트)

Phase 4 — 대시보드
  10. DashboardRepository (집계 JPQL)
  11. DashboardComponent + 차트 라이브러리 연동 (Chart.js 또는 ng2-charts)
```

---

## 9. 미결 사항 (팀 확인 필요)

| 번호 | 항목 | 옵션 |
|------|------|------|
| 1 | `unit` 컬럼 의미 | 공정 단위(P1, P2) vs 다른 개념 |
| 2 | 파일 저장소 | 로컬 파일시스템(현재) vs AWS S3 |
| 3 | `company_emp_hist` 활성화 시점 | 현재 단순화(emp.company_seq) vs 다중 소속 지원 |
| 4 | 대시보드 기간 필터 기본값 | Last 7 days(목업 기준) |
| 5 | Refresh Token 저장 위치 | DB vs Redis |
