# Qcheck

제조 현장의 불량(Defect)을 라인·부품·불량코드 단위로 기록·집계하는 **멀티테넌트 품질 SaaS**

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 3.4 · Java 17 · Spring Security · Spring Data JPA |
| Frontend | Angular 20 · Standalone Components · Angular Signals · RxJS |
| DB | PostgreSQL 16 (Docker) |
| 인증 | JWT (AccessToken 1일 / RefreshToken 7일) |

---

## 빠른 시작

### 사전 요구사항
- Java 17 (`/usr/lib/jvm/java-17-openjdk-amd64`)
- Node.js 18+
- Docker (PostgreSQL 컨테이너)

### 1. DB 실행
```bash
docker start qcheck-postgres
# 포트: 5433 / 사용자: postgres / 비밀번호: test / DB: qcheck
```

### 2. 스키마 초기화 (최초 1회)
```bash
docker exec -i qcheck-postgres psql -U postgres -d qcheck \
  < back/src/main/resources/database_setup.sql
```

### 3. 백엔드 실행
```bash
cd back
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./mvnw spring-boot:run
# → http://localhost:8081
```

### 4. 프론트엔드 실행
```bash
cd front
npm install
npm run start -- --host 0.0.0.0 --proxy-config proxy.conf.json
# → http://localhost:4200
```

---

## 프로젝트 구조

```
Qcheck/
├── back/                          # Spring Boot 백엔드
│   └── src/main/java/com/qcheck/qcheck/
│       ├── controller/            # REST API 컨트롤러
│       ├── service/               # 비즈니스 로직
│       │   ├── defect/
│       │   ├── master/
│       │   └── dashboard/
│       ├── repository/            # Spring Data JPA 레포지토리
│       ├── entity/                # JPA 엔티티
│       ├── dto/
│       │   ├── request/           # 요청 DTO
│       │   └── response/          # 응답 DTO
│       ├── security/              # JWT 필터, UserDetailsService
│       ├── config/                # SecurityConfig, WebMvcConfig
│       └── exception/             # 글로벌 예외 처리
│
└── front/                         # Angular 20 프론트엔드
    └── src/app/
        ├── dashboard/
        ├── defects/               # 불량 목록 · 등록/수정 폼
        ├── master/                # 라인 · 부품 · 공정단위 · 불량코드 관리
        ├── register/
        ├── services/              # API 호출 서비스
        ├── guards/                # AuthGuard
        └── interceptors/          # JwtInterceptor
```

---

## 도메인 모델

```
Company (1) ──── (N) Emp
    │
    ├── (1:N) Line        생산 라인
    ├── (1:N) Part        부품/제품
    ├── (1:N) Unit        공정 단위
    └── (1:N) DefectCode  불량 코드 마스터
                 │
                 └── (1:N) Defect  불량 기록
                              ├── FK: company_seq
                              ├── FK: line_seq
                              ├── FK: part_seq
                              ├── FK: unit_seq
                              └── FK: defect_code_seq
```

모든 도메인 데이터는 `company_seq`로 완전 격리됩니다 (멀티테넌트).

---

## 화면 구성

| 화면 | 경로 | 설명 |
|------|------|------|
| 로그인 | `/login` | 이메일 + 비밀번호 |
| 회원가입 | `/register` | 회사 + 관리자 계정 동시 생성 |
| 대시보드 | `/dashboard` | 기간별 불량 현황, 라인별/코드별 통계 |
| 불량 목록 | `/defects` | 날짜 범위 검색 · 편집 · 삭제 |
| 불량 등록 | `/defects/add` | 라인·부품·공정단위·불량코드·수량·이미지 입력 |
| 불량 수정 | `/defects/:seq/edit` | 기존 데이터 불러와 수정 |
| 마스터 관리 | `/master/*` | 라인·부품·공정단위·불량코드 CRUD |

---

## API 엔드포인트

### 인증 (공개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/login` | 로그인 (accessToken + refreshToken 발급) |
| POST | `/api/auth/register` | 회사 + 관리자 계정 등록 |
| POST | `/api/auth/refresh` | accessToken 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |

### 불량 관리 (인증 필요)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/defects?from=&to=&keyword=&page=&size=` | 불량 목록 (날짜 범위, 키워드, 페이징) |
| GET | `/api/defects/:seq` | 불량 상세 |
| POST | `/api/defects` | 불량 등록 (multipart/form-data) |
| PUT | `/api/defects/:seq` | 불량 수정 (multipart/form-data) |
| DELETE | `/api/defects/:seq` | 불량 삭제 |
| GET | `/api/defects/:seq/image` | 불량 이미지 다운로드 |

**불량 등록/수정 요청 형식**
```
Content-Type: multipart/form-data
- defect  (application/json): { lineSeq, partSeq, unitSeq, defectCodeSeq, quantity, position?, remarks?, occurDate }
- file    (선택): 이미지 파일 (jpg/jpeg/png/gif/bmp, 최대 10MB)
```

### 마스터 데이터 (인증 필요)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/master/lines` | 라인 목록 |
| POST | `/api/master/lines` | 라인 생성 |
| PUT | `/api/master/lines/:seq` | 라인 수정 |
| DELETE | `/api/master/lines/:seq` | 라인 삭제 |
| GET/POST/PUT/DELETE | `/api/master/parts` | 부품 관리 |
| GET/POST/PUT/DELETE | `/api/master/units` | 공정 단위 관리 |
| GET/POST/PUT/DELETE | `/api/master/defect-codes` | 불량 코드 관리 |

### 대시보드 (인증 필요)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/dashboard?from=&to=` | 기간별 통계 (라인별·코드별·일별) |

---

## 인증 흐름

```
[Login] → AccessToken(1일) + RefreshToken(7일) → localStorage 저장
   │
[모든 요청] JwtInterceptor → Authorization: Bearer <token> 헤더 자동 추가
   │
[백엔드] JwtAuthenticationFilter
   → 토큰 검증 → companySeq 추출
   → request.setAttribute("companySeq", companySeq)
   → Controller에서 @RequestAttribute("companySeq") 로 수신
   │
[401 응답] JwtInterceptor → refreshToken으로 자동 갱신 후 재요청
           갱신 실패 시 → 자동 로그아웃 → /login 리다이렉트
```

**JWT Payload**
```json
{
  "sub": "1",
  "companySeq": 1,
  "empRole": "USER",
  "email": "user@example.com"
}
```

---

## 데이터베이스 스키마

```sql
company        -- 고객사 (company_seq, name, status)
emp            -- 사원 (emp_seq, emp_id, emp_email, emp_pw, company_seq, emp_role, use_yn)
line           -- 생산 라인 (line_seq, company_seq, line_nm, use_yn)
part           -- 부품/제품 (part_seq, company_seq, part_nm, use_yn)
unit           -- 공정 단위 (unit_seq, company_seq, unit_nm, use_yn)
defect_code    -- 불량 코드 (defect_code_seq, company_seq, code_nm, description, use_yn)
defect         -- 불량 기록 (defect_seq, company_seq, line_seq, part_seq, unit_seq,
                --            defect_code_seq, quantity, position, remarks, occur_date, image_path)
```

모든 테이블에 Audit 컬럼 적용: `created_at`, `created_ep`, `updated_at`, `updated_ep`

**주요 인덱스**
```sql
idx_defect_company_date  ON defect(company_seq, occur_date DESC)
idx_defect_line          ON defect(company_seq, line_seq, occur_date DESC)
idx_defect_code          ON defect(company_seq, defect_code_seq, occur_date DESC)
```

---

## 주요 설정값 (application.properties)

| 설정 | 값 |
|------|----|
| 서버 포트 | `8081` |
| DB 포트 | `5433` |
| AccessToken 유효기간 | `86400000` ms (1일) |
| RefreshToken 유효기간 | `604800000` ms (7일) |
| 이미지 저장 경로 | `/files/defects` |
| CORS 허용 | `http://localhost:4200` |

---

## 이미지

![QCheck 화면 구성](image/QCheck_example.png)
