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
-- 고객사 정보를 저장하는 메인 테이블
CREATE TABLE IF NOT EXISTS company (
    -- 기본키: 자동 증가 정수
    id              BIGSERIAL PRIMARY KEY,

    -- 고객사명: 필수 입력, 최대 100자
    name            VARCHAR(100) NOT NULL,

    -- 고객사 상태: 열거형 값 (ACTIVE, INACTIVE, SUSPENDED)
    status          VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),

    -- 생성 시간: 자동 설정
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- 수정 시간: 자동 갱신
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- 6. 인덱스 생성
-- -----------------------------------------------------
-- 고객사명으로 검색 성능 향상
CREATE INDEX IF NOT EXISTS idx_company_name ON company(name);

-- 상태별 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_company_status ON company(status);

-- -----------------------------------------------------
-- 7. 테이블 주석 추가
-- -----------------------------------------------------
COMMENT ON TABLE company IS '고객사 정보 테이블';
COMMENT ON COLUMN company.id IS '고객사 고유 식별자 (기본키)';
COMMENT ON COLUMN company.name IS '고객사명 (최대 100자)';
COMMENT ON COLUMN company.status IS '고객사 상태 (ACTIVE: 활성, INACTIVE: 비활성, SUSPENDED: 일시중단)';
COMMENT ON COLUMN company.created_at IS '생성 일시';
COMMENT ON COLUMN company.updated_at IS '최종 수정 일시';

-- -----------------------------------------------------
-- 8. 트리거 생성 (updated_at 자동 갱신)
-- -----------------------------------------------------
-- updated_at 필드를 자동으로 현재 시간으로 갱신하는 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- company 테이블에 트리거 적용
DROP TRIGGER IF EXISTS update_company_updated_at ON company;
CREATE TRIGGER update_company_updated_at
    BEFORE UPDATE ON company
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------
-- 9. 샘플 데이터 삽입 (개발/테스트용)
-- -----------------------------------------------------
-- 테스트용 고객사 데이터 삽입
INSERT INTO company (name, status) VALUES
    ('테크 코퍼레이션', 'ACTIVE'),
    ('글로벌 시스템즈', 'ACTIVE'),
    ('이노베이션 랩', 'INACTIVE')
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------
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