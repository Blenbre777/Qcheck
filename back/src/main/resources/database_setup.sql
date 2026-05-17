-- =====================================================
-- QCheck 데이터베이스 생성 및 설정 스크립트
-- =====================================================
-- 목적: QCheck 애플리케이션을 위한 PostgreSQL 데이터베이스 환경 구성
-- 실행 순서: 1) 데이터베이스 생성 → 2) 사용자 생성 → 3) 권한 부여 → 4) 테이블 생성
-- 사용법: psql -U postgres -f database_setup.sql

-- -----------------------------------------------------
-- 1. 데이터베이스 생성
-- -----------------------------------------------------
-- 기존 연결 종료 (필요 시)
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'qcheck'
  AND pid <> pg_backend_pid();

-- 기존 데이터베이스 삭제 (존재하는 경우)
DROP DATABASE IF EXISTS qcheck;

-- 새 데이터베이스 생성
CREATE DATABASE qcheck
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 데이터베이스에 주석 추가
COMMENT ON DATABASE qcheck IS 'QCheck 품질 관리 SaaS 애플리케이션 데이터베이스';

-- -----------------------------------------------------
-- 2. 데이터베이스 연결 전환
-- -----------------------------------------------------
-- 주의: 이 명령어는 psql에서 실행해야 함
-- \c qcheck;

-- -----------------------------------------------------
-- 3. 확장 프로그램 설치 (UUID 지원)
-- -----------------------------------------------------
-- UUID 생성을 위한 확장 (향후 사용 대비)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------
-- 4. 스키마 생성 (필요시)
-- -----------------------------------------------------
-- 기본적으로 public 스키마 사용
-- 향후 확장 시 추가 스키마 생성 가능

-- -----------------------------------------------------
-- 5. Company 테이블 생성
-- -----------------------------------------------------

-- 고객사 (COMPANY) 를 저장하는 메인 테이블
CREATE TABLE IF NOT EXISTS company (
    -- 기본키: 자동 증가 정수
    company_seq     BIGSERIAL PRIMARY KEY,
    -- 고객사명: 필수 입력, 최대 100자
    name            VARCHAR(100) NOT NULL,
    -- 고객사 상태: 열거형 값 (ACTIVE, INACTIVE, SUSPENDED)
    status          VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    -- 생성 시간: 자동 입력
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,    
    -- 생성자: 기본값 SYSTEM
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'TEST',  -- 이거 TEST 로 생성자 넣어두고 추후 사용자 emp_id 가져와서 수정자가 누군지 확인 가능하도록 하기 윟마    
    -- 수정 시간: 최초 NULL, 직접 업데이트 시 값 입력
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,    
    -- 수정자: 최초 NULL, 직접 업데이트 시 값 입력
    updated_ep  VARCHAR(50) DEFAULT NULL
);

-- 고객사 (USER) 를 저장하는 메인 테이블
CREATE TABLE IF NOT EXISTS emp (
    -- 기본키: 자동 증가 정수
    emp_seq     BIGSERIAL PRIMARY KEY,
    -- 로그인 아이디: 중복 불가, 필수 입력
    emp_id      VARCHAR(50) NOT NULL UNIQUE,
    -- 로그인 비밀번호: 암호화 저장 전제
    emp_pw      VARCHAR(255) NOT NULL,
    -- 사원 이름
    emp_nm      VARCHAR(100) NOT NULL,
    -- 소속 회사: 외래키, 멀티테넌트 격리 필수
    company_seq BIGINT NOT NULL REFERENCES company(company_seq),
    -- 사원 이메일: 중복 불가, JWT 토큰에 사용
    emp_email   VARCHAR(255) NOT NULL UNIQUE,
    -- 사원 권한: SUPER_ADMIN, COMPANY_ADMIN, USER (기본값: USER)
    emp_role    VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (emp_role IN ('SUPER_ADMIN', 'COMPANY_ADMIN', 'USER')),
    -- 사용 여부 (Y/N)
    use_yn      VARCHAR(2) NOT NULL DEFAULT 'Y',
    -- 생성 시간: 자동 입력
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자: 기본값 SYSTEM
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',    -- 회원가입시 system
    -- 수정 시간: 최초 NULL, 직접 업데이트 시 값 입력
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자: 최초 NULL, 직접 업데이트 시 값 입력
    updated_ep  VARCHAR(50) DEFAULT NULL
);

-- 고객사-사용자 이력(소속) 테이블
-- HS : 이거 아직사용 x , 이하 SQL 사용 X 
CREATE TABLE IF NOT EXISTS company_emp_hist (
    -- 기본키: 자동 증가 정수
    hist_seq    BIGSERIAL PRIMARY KEY,
    
    -- 외래키: 회사 식별자
    company_seq BIGINT NOT NULL,
    -- 외래키: 사용자 식별자
    emp_seq     BIGINT NOT NULL,
    
    -- 소속 시작일
    start_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    -- 소속 종료일 (NULL이면 현재 소속 중)
    end_date    DATE DEFAULT NULL,
    
    -- 생성/수정 관리 컬럼
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    updated_ep  VARCHAR(50) DEFAULT NULL,
    
    -- 제약조건: 외래키 설정
    CONSTRAINT fk_company FOREIGN KEY (company_seq) REFERENCES company(company_seq),
    CONSTRAINT fk_emp FOREIGN KEY (emp_seq) REFERENCES emp(emp_seq),
    -- 회사-사용자 중복 방지 (동일 회사에 동일 사용자 중복 등록 불가)
    CONSTRAINT uq_company_emp UNIQUE (company_seq, emp_seq, start_date)
);

-- -----------------------------------------------------
-- 6. 인덱스 생성
-- 9.28 HS 보류요청:인덱스 사용까지 좋은 방법인거 같으나 , 현재로써 컬럼 10개 이내와 데이터 조회 건수 100만건 아래라 사용 의미 미미하다 생각돼서 보류하는게 좋다고 생각함
-- -----------------------------------------------------
-- 고객사명으로 검색 성능 향상
CREATE INDEX IF NOT EXISTS idx_company_name ON company(name);

-- 상태별 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_company_status ON company(status);

-- -----------------------------------------------------
-- 7. 테이블 주석 추가
-- -----------------------------------------------------
--회사 메인 테이블
COMMENT ON TABLE company             IS '고객사 정보 테이블';
COMMENT ON COLUMN company.id         IS '고객사 고유 식별자 (기본키)';
COMMENT ON COLUMN company.name       IS '고객사명 (최대 100자)';
COMMENT ON COLUMN company.status     IS '고객사 상태 (ACTIVE: 활성, INACTIVE: 비활성, SUSPENDED: 일시중단)';
COMMENT ON COLUMN company.created_at IS '생성 일시';
COMMENT ON COLUMN company.created_ep IS '생성자';
COMMENT ON COLUMN company.updated_at IS '수정 일시';
COMMENT ON COLUMN company.updated_ep IS '수정자';

--고객 정보 메인 테이블
COMMENT ON TABLE emp             IS '사원(사용자) 정보 메인 테이블';
COMMENT ON COLUMN emp.emp_seq    IS '기본키: 자동 증가 정수 (사원 일련번호)';
COMMENT ON COLUMN emp.emp_id     IS '로그인 아이디: 중복 불가, 필수 입력';
COMMENT ON COLUMN emp.emp_pw     IS '로그인 비밀번호: 암호화 저장 전제';
COMMENT ON COLUMN emp.emp_nm     IS '사원 이름';
COMMENT ON COLUMN emp.use_yn     IS '사용 여부 (Y=사용, N=미사용), 기본값 Y';
COMMENT ON COLUMN emp.created_at IS '생성 시간: 회원가입 시 자동 입력';
COMMENT ON COLUMN emp.created_ep IS '생성자: 기본값 SYSTEM';
COMMENT ON COLUMN emp.updated_at IS '수정 시간: 최초 NULL, 이후 정보 변경 시 직접 입력';
COMMENT ON COLUMN emp.updated_ep IS '수정자: 최초 NULL, 이후 정보 변경 시 직접 입력';

--

-- -----------------------------------------------------
-- 8. 트리거 생성 (updated_at 자동 갱신)
-- 9.27 HS 삭제 요청:updated_at 트리거 사용시 request 과정에서 rock 방지 (post..SQL?? 은 모르겠으나 , 트랜잭션 처리꼬임 방지용) GPT 가 쓴거 같은데 이건 사용 비추천
-- -----------------------------------------------------
-- updated_at 필드를 자동으로 현재 시간으로 갱신하는 함수
--CREATE OR REPLACE FUNCTION update_updated_at_column()
--RETURNS TRIGGER AS $$
--BEGIN
--    NEW.updated_at = CURRENT_TIMESTAMP;
--    RETURN NEW;
--END;
--$$ language 'plpgsql';

-- company 테이블에 트리거 적용
--DROP TRIGGER IF EXISTS update_company_updated_at ON company;
--CREATE TRIGGER update_company_updated_at
--    BEFORE UPDATE ON company
--    FOR EACH ROW
--    EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------
-- 9. 샘플 데이터 삽입 (개발/테스트용)
-- -----------------------------------------------------
-- 테스트용 고객사 데이터 삽입
INSERT INTO company (name, status) VALUES
    ('테크 코퍼레이션', 'ACTIVE'),
    ('글로벌 시스템즈', 'ACTIVE'),
    ('이노베이션 랩', 'INACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO emp (emp_id, emp_pw, emp_nm, use_yn, created_ep)
VALUES ({사용하는 id}, {사용하는 pw}, '테스트계정', 'Y', 'SYSTEM'); 

-- 예: '글로벌 시스템즈' 회사에 'test_user' 사용자를 오늘자로 소속 등록
-- HS:이거 오류날 수 있음 참고만 부탁
INSERT INTO company_emp_hist (company_seq, emp_seq, start_date, created_ep)
SELECT  c.company_seq,
        e.emp_seq,
        CURRENT_DATE,
        'SYSTEM'
FROM company c
JOIN emp     e ON e.emp_id = 'test_user'         -- ← 사용하는 id
WHERE c.name = '글로벌 시스템즈'                    -- ← 대상 회사명
ON CONFLICT DO NOTHING;

-- =====================================================
-- PHASE 2-4: 신규 테이블 DDL (생산 라인, 부품, 공정, 불량 마스터, 불량 기록)
-- =====================================================

-- 생산 라인 (LINE) 테이블
CREATE TABLE IF NOT EXISTS line (
    -- 기본키: 자동 증가 정수
    line_seq    BIGSERIAL PRIMARY KEY,
    -- 외래키: 회사 식별자 (멀티테넌트)
    company_seq BIGINT NOT NULL REFERENCES company(company_seq) ON DELETE CASCADE,
    -- 생산 라인명
    line_nm     VARCHAR(100) NOT NULL,
    -- 사용 여부 (Y/N)
    use_yn      VARCHAR(2) NOT NULL DEFAULT 'Y',
    -- 생성 시간
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    -- 수정 시간
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자
    updated_ep  VARCHAR(50) DEFAULT NULL,
    -- 같은 회사 내에서 라인명 중복 방지
    CONSTRAINT uq_line_company_name UNIQUE (company_seq, line_nm)
);

-- 부품/제품 (PART) 테이블
CREATE TABLE IF NOT EXISTS part (
    -- 기본키: 자동 증가 정수
    part_seq    BIGSERIAL PRIMARY KEY,
    -- 외래키: 회사 식별자 (멀티테넌트)
    company_seq BIGINT NOT NULL REFERENCES company(company_seq) ON DELETE CASCADE,
    -- 부품/제품명
    part_nm     VARCHAR(100) NOT NULL,
    -- 사용 여부 (Y/N)
    use_yn      VARCHAR(2) NOT NULL DEFAULT 'Y',
    -- 생성 시간
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    -- 수정 시간
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자
    updated_ep  VARCHAR(50) DEFAULT NULL,
    -- 같은 회사 내에서 부품명 중복 방지
    CONSTRAINT uq_part_company_name UNIQUE (company_seq, part_nm)
);

-- 공정 단위 (UNIT) 테이블
CREATE TABLE IF NOT EXISTS unit (
    -- 기본키: 자동 증가 정수
    unit_seq    BIGSERIAL PRIMARY KEY,
    -- 외래키: 회사 식별자 (멀티테넌트)
    company_seq BIGINT NOT NULL REFERENCES company(company_seq) ON DELETE CASCADE,
    -- 공정 단위명 (예: P1, P2, Assembly, Inspection)
    unit_nm     VARCHAR(100) NOT NULL,
    -- 사용 여부 (Y/N)
    use_yn      VARCHAR(2) NOT NULL DEFAULT 'Y',
    -- 생성 시간
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자
    created_ep  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    -- 수정 시간
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자
    updated_ep  VARCHAR(50) DEFAULT NULL,
    -- 같은 회사 내에서 공정명 중복 방지
    CONSTRAINT uq_unit_company_name UNIQUE (company_seq, unit_nm)
);

-- 불량 코드 마스터 (DEFECT_CODE) 테이블
CREATE TABLE IF NOT EXISTS defect_code (
    -- 기본키: 자동 증가 정수
    defect_code_seq BIGSERIAL PRIMARY KEY,
    -- 외래키: 회사 식별자 (멀티테넌트)
    company_seq     BIGINT NOT NULL REFERENCES company(company_seq) ON DELETE CASCADE,
    -- 불량 코드명 (예: 색상, 흠집, 변형)
    code_nm         VARCHAR(100) NOT NULL,
    -- 불량 코드 설명
    description     TEXT,
    -- 사용 여부 (Y/N)
    use_yn          VARCHAR(2) NOT NULL DEFAULT 'Y',
    -- 생성 시간
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자
    created_ep      VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    -- 수정 시간
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자
    updated_ep      VARCHAR(50) DEFAULT NULL,
    -- 같은 회사 내에서 불량 코드명 중복 방지
    CONSTRAINT uq_defect_code_company_name UNIQUE (company_seq, code_nm)
);

-- 불량 기록 (DEFECT) 테이블
CREATE TABLE IF NOT EXISTS defect (
    -- 기본키: 자동 증가 정수
    defect_seq      BIGSERIAL PRIMARY KEY,
    -- 외래키: 회사 식별자 (멀티테넌트, 데이터 격리)
    company_seq     BIGINT NOT NULL REFERENCES company(company_seq) ON DELETE CASCADE,
    -- 외래키: 생산 라인
    line_seq        BIGINT REFERENCES line(line_seq) ON DELETE SET NULL,
    -- 외래키: 부품/제품
    part_seq        BIGINT REFERENCES part(part_seq) ON DELETE SET NULL,
    -- 외래키: 공정 단위
    unit_seq        BIGINT REFERENCES unit(unit_seq) ON DELETE SET NULL,
    -- 외래키: 불량 코드 마스터
    defect_code_seq BIGINT NOT NULL REFERENCES defect_code(defect_code_seq) ON DELETE RESTRICT,
    -- 불량 수량 (기본값 1)
    quantity        INTEGER DEFAULT 1,
    -- 불량 위치 (예: 좌상단, 중앙 등)
    position        VARCHAR(100),
    -- 비고/상세 설명
    remarks         TEXT,
    -- 불량 발생 날짜
    occur_date      DATE NOT NULL,
    -- 불량 이미지 저장 경로 (로컬 파일 시스템)
    image_path      VARCHAR(500),
    -- 생성 시간
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- 생성자
    created_ep      VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    -- 수정 시간
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    -- 수정자
    updated_ep      VARCHAR(50) DEFAULT NULL
);

-- 불량 관련 인덱스
-- 회사별, 발생 날짜별 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_defect_company_date ON defect(company_seq, occur_date DESC);

-- 회사별, 라인별, 날짜별 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_defect_line ON defect(company_seq, line_seq, occur_date DESC);

-- 회사별, 불량 코드별, 날짜별 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_defect_code ON defect(company_seq, defect_code_seq, occur_date DESC);

-- 라인, 부품, 공정 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_line_company ON line(company_seq);
CREATE INDEX IF NOT EXISTS idx_part_company ON part(company_seq);
CREATE INDEX IF NOT EXISTS idx_unit_company ON unit(company_seq);
CREATE INDEX IF NOT EXISTS idx_defect_code_company ON defect_code(company_seq);

-- 테이블 주석 추가
COMMENT ON TABLE line IS '생산 라인 마스터';
COMMENT ON TABLE part IS '부품/제품 마스터';
COMMENT ON TABLE unit IS '공정 단위 마스터';
COMMENT ON TABLE defect_code IS '불량 코드 마스터';
COMMENT ON TABLE defect IS '불량 기록';

-- 컬럼 주석 추가
COMMENT ON COLUMN defect.company_seq IS '데이터 격리: 같은 회사의 데이터만 조회 가능';
COMMENT ON COLUMN defect.occur_date IS '불량 발생 날짜: 대시보드 집계 기준';
COMMENT ON COLUMN defect.image_path IS '로컬 파일 시스템 경로: /files/defects/{company_seq}/{yyyy-MM}/';

-- =====================================================
-- 10. 권한 설정 (필요시)
-- -----------------------------------------------------
-- 애플리케이션 전용 사용자 생성 및 권한 부여
-- CREATE USER qcheck_user WITH PASSWORD 'your_secure_password';
-- GRANT CONNECT ON DATABASE qcheck TO qcheck_user;
-- GRANT USAGE ON SCHEMA public TO qcheck_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO qcheck_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO qcheck_user;

-- -----------------------------------------------------
-- 11. 설정 완료 메시지
-- -----------------------------------------------------
SELECT 'QCheck 데이터베이스 설정이 완료되었습니다!' AS message;
SELECT 'Company 테이블 생성 및 샘플 데이터 삽입 완료' AS status;