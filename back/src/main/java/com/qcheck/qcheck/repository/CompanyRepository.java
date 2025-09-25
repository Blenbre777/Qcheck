package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Company;
import com.qcheck.qcheck.entity.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company 엔터티를 위한 데이터 접근 계층 (Repository)
 *
 * 목적: Company 테이블과 상호작용하는 데이터베이스 접근 메서드 제공
 * 상속: JpaRepository<Entity클래스, 기본키타입>를 상속받아 기본 CRUD 자동 제공
 *
 * 기본 제공 메서드 (JpaRepository에서 상속):
 * - save(Company) : 저장/수정
 * - findById(Long) : ID로 조회
 * - findAll() : 전체 조회
 * - delete(Company) : 삭제
 * - count() : 총 개수
 *
 * 커스텀 메서드:
 * - JPA가 메서드 이름을 분석해서 자동으로 SQL 쿼리 생성
 * - @Query 어노테이션으로 직접 쿼리 작성도 가능
 *
 * 사용 예시:
 * Company company = companyRepository.findById(1L).orElse(null);
 * List<Company> activeCompanies = companyRepository.findByStatus(CompanyStatus.ACTIVE);
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // ========================================
    // 1. 기본 조회 메서드들 (메서드명 기반 쿼리)
    // ========================================

    /**
     * 회사 상태로 조회
     *
     * 메서드명 규칙: findBy + 필드명(Status)
     * 자동 생성 SQL: SELECT * FROM company WHERE status = ?
     *
     * @param status 조회할 회사 상태 (ACTIVE, INACTIVE, SUSPENDED)
     * @return 해당 상태의 회사 목록
     *
     * 사용 예시:
     * List<Company> activeCompanies = repository.findByStatus(CompanyStatus.ACTIVE);
     */
    List<Company> findByStatus(CompanyStatus status);

    /**
     * 회사명으로 조회 (정확히 일치)
     *
     * 메서드명 규칙: findBy + 필드명(Name)
     * 자동 생성 SQL: SELECT * FROM company WHERE name = ?
     *
     * @param name 조회할 회사명 (정확한 이름)
     * @return 해당 이름의 회사 (Optional로 감싸서 null 안전)
     *
     * 사용 예시:
     * Optional<Company> company = repository.findByName("테크 코퍼레이션");
     * if (company.isPresent()) { ... }
     */
    Optional<Company> findByName(String name);

    /**
     * 회사명에 특정 키워드가 포함된 회사 조회
     *
     * 메서드명 규칙: findBy + 필드명 + Containing
     * 자동 생성 SQL: SELECT * FROM company WHERE name LIKE %키워드%
     *
     * @param keyword 검색할 키워드
     * @return 회사명에 키워드가 포함된 회사들
     *
     * 사용 예시:
     * List<Company> companies = repository.findByNameContaining("테크");
     * // "테크 코퍼레이션", "글로벌 테크" 등이 검색됨
     */
    List<Company> findByNameContaining(String keyword);

    /**
     * 특정 상태가 아닌 회사들 조회
     *
     * 메서드명 규칙: findBy + 필드명 + Not
     * 자동 생성 SQL: SELECT * FROM company WHERE status != ?
     *
     * @param status 제외할 상태
     * @return 해당 상태가 아닌 회사들
     *
     * 사용 예시:
     * List<Company> companies = repository.findByStatusNot(CompanyStatus.INACTIVE);
     * // ACTIVE, SUSPENDED 상태의 회사들만 조회
     */
    List<Company> findByStatusNot(CompanyStatus status);

    // ========================================
    // 2. 복합 조건 조회 메서드들
    // ========================================

    /**
     * 회사명과 상태로 함께 조회
     *
     * 메서드명 규칙: findBy + 필드1 + And + 필드2
     * 자동 생성 SQL: SELECT * FROM company WHERE name LIKE %?% AND status = ?
     *
     * @param nameKeyword 회사명에서 검색할 키워드
     * @param status 회사 상태
     * @return 조건을 만족하는 회사들
     *
     * 사용 예시:
     * List<Company> companies = repository.findByNameContainingAndStatus("코퍼", CompanyStatus.ACTIVE);
     */
    List<Company> findByNameContainingAndStatus(String nameKeyword, CompanyStatus status);

    // ========================================
    // 3. 정렬된 조회 메서드들
    // ========================================

    /**
     * 모든 회사를 이름 순으로 정렬해서 조회
     *
     * 메서드명 규칙: findAllBy + OrderBy + 필드명 + Asc/Desc
     * 자동 생성 SQL: SELECT * FROM company ORDER BY name ASC
     *
     * @return 이름 순으로 정렬된 모든 회사들
     *
     * 사용 예시:
     * List<Company> companies = repository.findAllByOrderByNameAsc();
     */
    List<Company> findAllByOrderByNameAsc();

    /**
     * 특정 상태의 회사들을 이름 순으로 조회
     *
     * @param status 조회할 상태
     * @return 이름 순으로 정렬된 해당 상태의 회사들
     */
    List<Company> findByStatusOrderByNameAsc(CompanyStatus status);

    // ========================================
    // 4. 개수 조회 메서드들
    // ========================================

    /**
     * 특정 상태의 회사 개수 조회
     *
     * 메서드명 규칙: countBy + 필드명
     * 자동 생성 SQL: SELECT COUNT(*) FROM company WHERE status = ?
     *
     * @param status 개수를 셀 상태
     * @return 해당 상태의 회사 개수
     *
     * 사용 예시:
     * long activeCount = repository.countByStatus(CompanyStatus.ACTIVE);
     */
    long countByStatus(CompanyStatus status);

    /**
     * 회사명에 키워드가 포함된 회사 개수 조회
     *
     * @param keyword 검색할 키워드
     * @return 조건을 만족하는 회사 개수
     */
    long countByNameContaining(String keyword);

    // ========================================
    // 5. 존재 여부 확인 메서드들
    // ========================================

    /**
     * 특정 이름의 회사가 존재하는지 확인
     *
     * 메서드명 규칙: existsBy + 필드명
     * 자동 생성 SQL: SELECT EXISTS(SELECT 1 FROM company WHERE name = ?)
     *
     * @param name 확인할 회사명
     * @return 존재하면 true, 없으면 false
     *
     * 사용 예시:
     * boolean exists = repository.existsByName("테크 코퍼레이션");
     * if (!exists) { // 중복 체크 }
     */
    boolean existsByName(String name);

    // ========================================
    // 6. 커스텀 쿼리 메서드들 (@Query 사용)
    // ========================================

    /**
     * 활성 상태인 회사들만 조회 (커스텀 JPQL 쿼리)
     *
     * @Query: JPQL(Java Persistence Query Language) 직접 작성
     * - SQL과 유사하지만 테이블명 대신 엔터티 클래스명 사용
     * - 컬럼명 대신 엔터티 필드명 사용
     *
     * @return 활성 상태인 모든 회사들
     *
     * 사용 예시:
     * List<Company> activeCompanies = repository.findActiveCompanies();
     */
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE'")
    List<Company> findActiveCompanies();

    /**
     * 회사명으로 대소문자 구분 없이 검색 (커스텀 JPQL)
     *
     * @param name 검색할 회사명 (대소문자 무관)
     * @return 대소문자 구분 없이 일치하는 회사들
     *
     * 사용 예시:
     * List<Company> companies = repository.findByNameIgnoreCase("TECH");
     * // "tech", "Tech", "TECH" 모두 검색됨
     */
    @Query("SELECT c FROM Company c WHERE UPPER(c.name) LIKE UPPER(CONCAT('%', :name, '%'))")
    List<Company> findByNameIgnoreCase(@Param("name") String name);

    /**
     * 네이티브 SQL 쿼리 예시
     *
     * nativeQuery = true: 실제 SQL 쿼리 사용 (PostgreSQL 문법)
     * JPQL 대신 순수 SQL을 작성할 때 사용
     *
     * @return 회사 개수 통계
     */
    @Query(value = "SELECT COUNT(*) FROM company WHERE status = 'ACTIVE'", nativeQuery = true)
    long countActiveCompaniesNative();

    // ========================================
    // 7. 학습용 메서드명 패턴 정리
    // ========================================

    /*
     * JPA Repository 메서드명 작성 규칙:
     *
     * 1. 기본 패턴:
     *    - findBy + 필드명: 조건으로 조회
     *    - countBy + 필드명: 조건으로 개수
     *    - existsBy + 필드명: 존재 여부 확인
     *    - deleteBy + 필드명: 조건으로 삭제
     *
     * 2. 조건 연산자:
     *    - Containing: LIKE %값%
     *    - StartingWith: LIKE 값%
     *    - EndingWith: LIKE %값
     *    - IgnoreCase: 대소문자 무시
     *    - Not: != 조건
     *    - GreaterThan: > 조건
     *    - LessThan: < 조건
     *
     * 3. 복합 조건:
     *    - And: WHERE 조건1 AND 조건2
     *    - Or: WHERE 조건1 OR 조건2
     *
     * 4. 정렬:
     *    - OrderBy + 필드명 + Asc: 오름차순
     *    - OrderBy + 필드명 + Desc: 내림차순
     *
     * 5. 제한:
     *    - First, Top: 상위 몇 개만 조회
     *    - 예: findFirst5ByOrderByNameAsc()
     */
}