package com.qcheck.qcheck.controller;

import com.qcheck.qcheck.entity.Company;
import com.qcheck.qcheck.entity.CompanyStatus;
import com.qcheck.qcheck.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Company 관련 REST API 컨트롤러 (테스트용 간단 버전)
 *
 * 목적: CompanyRepository의 기능을 테스트하기 위한 간단한 REST 엔드포인트 제공
 * 실제 운영에서는 Service 계층을 거쳐야 하지만, 학습용으로 직접 Repository 사용
 *
 * 주요 기능:
 * - Repository의 SELECT 메서드들을 HTTP API로 노출
 * - 브라우저나 Postman에서 쉽게 테스트 가능
 * - JSON 형태로 응답 반환
 *
 * 테스트 방법:
 * 1. Spring Boot 애플리케이션 실행 (포트 8081)
 * 2. 브라우저에서 http://localhost:8081/api/companies/all 접속
 * 3. 샘플 데이터가 JSON으로 출력되는지 확인
 *
 * API 엔드포인트 목록:
 * - GET /api/companies/all : 전체 회사 조회
 * - GET /api/companies/{id} : ID로 회사 조회
 * - GET /api/companies/status/{status} : 상태별 회사 조회
 * - GET /api/companies/search?keyword=검색어 : 회사명 검색
 * - GET /api/companies/count : 전체 회사 개수
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    /**
     * CompanyRepository 의존성 주입
     *
     * @Autowired: Spring이 자동으로 CompanyRepository 구현체를 주입
     * - 개발자가 직접 객체 생성하지 않아도 됨
     * - Spring이 JPA Repository 인터페이스의 구현체를 자동 생성
     */
    @Autowired
    private CompanyRepository companyRepository;

    // ========================================
    // 1. 기본 조회 API들
    // ========================================

    /**
     * 모든 회사 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/all
     * 응답: JSON 배열 형태의 모든 회사 데이터
     *
     * 테스트 방법:
     * curl http://localhost:8081/api/companies/all
     * 또는 브라우저에서 직접 접속
     *
     * @return 모든 회사들의 목록
     */
    @GetMapping("/all")
    public List<Company> getAllCompanies() {
        // Repository의 findAll() 메서드 호출
        // JPA가 자동으로 "SELECT * FROM company" 쿼리 실행
        return companyRepository.findAll();
    }

    /**
     * ID로 특정 회사 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/{id}
     * 경로 변수: {id} - 조회할 회사 ID
     *
     * 테스트 방법:
     * curl http://localhost:8081/api/companies/1
     *
     * @param id 조회할 회사 ID
     * @return 회사 정보 또는 404 에러
     */
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        // Repository의 findById() 메서드 호출
        // 반환값이 Optional<Company>이므로 null 안전 처리
        Optional<Company> company = companyRepository.findById(id);

        if (company.isPresent()) {
            // 회사가 존재하면 200 OK와 함께 데이터 반환
            return ResponseEntity.ok(company.get());
        } else {
            // 회사가 없으면 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 회사 상태별 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/status/{status}
     * 경로 변수: {status} - ACTIVE, INACTIVE, SUSPENDED 중 하나
     *
     * 테스트 방법:
     * curl http://localhost:8081/api/companies/status/ACTIVE
     *
     * @param status 조회할 회사 상태
     * @return 해당 상태의 회사들
     */
    @GetMapping("/status/{status}")
    public List<Company> getCompaniesByStatus(@PathVariable CompanyStatus status) {
        // Repository의 커스텀 메서드 findByStatus() 호출
        // JPA가 자동으로 "SELECT * FROM company WHERE status = ?" 쿼리 생성
        return companyRepository.findByStatus(status);
    }

    // ========================================
    // 2. 검색 API들
    // ========================================

    /**
     * 회사명으로 검색
     *
     * HTTP Method: GET
     * URL: /api/companies/search?keyword=검색어
     * 쿼리 파라미터: keyword - 검색할 키워드
     *
     * 테스트 방법:
     * curl "http://localhost:8081/api/companies/search?keyword=테크"
     *
     * @param keyword 회사명에서 검색할 키워드
     * @return 키워드가 포함된 회사들
     */
    @GetMapping("/search")
    public List<Company> searchCompanies(@RequestParam String keyword) {
        // Repository의 findByNameContaining() 메서드 호출
        // JPA가 자동으로 "SELECT * FROM company WHERE name LIKE %keyword%" 쿼리 생성
        return companyRepository.findByNameContaining(keyword);
    }

    /**
     * 정확한 회사명으로 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/name?exact=정확한회사명
     *
     * @param exactName 정확한 회사명
     * @return 해당 이름의 회사 또는 404
     */
    @GetMapping("/name")
    public ResponseEntity<Company> getCompanyByExactName(@RequestParam("exact") String exactName) {
        Optional<Company> company = companyRepository.findByName(exactName);

        return company.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // ========================================
    // 3. 통계 API들
    // ========================================

    /**
     * 전체 회사 개수 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/count
     *
     * @return 전체 회사 개수
     */
    @GetMapping("/count")
    public long getTotalCompanyCount() {
        // Repository의 count() 메서드 호출 (JpaRepository에서 기본 제공)
        return companyRepository.count();
    }

    /**
     * 상태별 회사 개수 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/count/status/{status}
     *
     * @param status 개수를 셀 상태
     * @return 해당 상태의 회사 개수
     */
    @GetMapping("/count/status/{status}")
    public long getCompanyCountByStatus(@PathVariable CompanyStatus status) {
        // Repository의 커스텀 메서드 countByStatus() 호출
        return companyRepository.countByStatus(status);
    }

    // ========================================
    // 4. 정렬된 조회 API들
    // ========================================

    /**
     * 모든 회사를 이름순으로 정렬해서 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/sorted
     *
     * @return 이름 오름차순으로 정렬된 모든 회사들
     */
    @GetMapping("/sorted")
    public List<Company> getAllCompaniesSorted() {
        // Repository의 findAllByOrderByNameAsc() 메서드 호출
        return companyRepository.findAllByOrderByNameAsc();
    }

    /**
     * 특정 상태의 회사들을 이름순으로 조회
     *
     * HTTP Method: GET
     * URL: /api/companies/sorted/status/{status}
     *
     * @param status 조회할 상태
     * @return 이름순으로 정렬된 해당 상태의 회사들
     */
    @GetMapping("/sorted/status/{status}")
    public List<Company> getCompaniesByStatusSorted(@PathVariable CompanyStatus status) {
        return companyRepository.findByStatusOrderByNameAsc(status);
    }

    // ========================================
    // 5. 커스텀 쿼리 테스트 API들
    // ========================================

    /**
     * 활성 회사들만 조회 (커스텀 JPQL 쿼리)
     *
     * HTTP Method: GET
     * URL: /api/companies/active
     *
     * @return 활성 상태인 모든 회사들
     */
    @GetMapping("/active")
    public List<Company> getActiveCompanies() {
        // Repository의 @Query 어노테이션으로 작성한 커스텀 메서드 호출
        return companyRepository.findActiveCompanies();
    }

    /**
     * 대소문자 구분 없이 회사명 검색
     *
     * HTTP Method: GET
     * URL: /api/companies/search-ignore-case?name=검색어
     *
     * @param name 검색할 회사명 (대소문자 무관)
     * @return 대소문자 구분 없이 일치하는 회사들
     */
    @GetMapping("/search-ignore-case")
    public List<Company> searchCompaniesIgnoreCase(@RequestParam String name) {
        return companyRepository.findByNameIgnoreCase(name);
    }

    // ========================================
    // 6. 유틸리티 API들
    // ========================================

    /**
     * 특정 회사명이 존재하는지 확인
     *
     * HTTP Method: GET
     * URL: /api/companies/exists?name=회사명
     *
     * @param name 확인할 회사명
     * @return 존재 여부 (true/false)
     */
    @GetMapping("/exists")
    public boolean checkCompanyExists(@RequestParam String name) {
        return companyRepository.existsByName(name);
    }

    /**
     * API 테스트를 위한 헬프 엔드포인트
     *
     * HTTP Method: GET
     * URL: /api/companies/help
     *
     * @return 사용 가능한 API 목록
     */
    @GetMapping("/help")
    public String getApiHelp() {
        return """
                QCheck Company API 테스트 가이드:

                📋 전체 조회:
                GET /api/companies/all - 모든 회사 조회
                GET /api/companies/sorted - 이름순 정렬 조회

                🔍 개별 조회:
                GET /api/companies/{id} - ID로 조회 (예: /api/companies/1)
                GET /api/companies/name?exact=회사명 - 정확한 이름으로 조회

                📊 상태별 조회:
                GET /api/companies/status/{status} - 상태별 조회 (ACTIVE, INACTIVE, SUSPENDED)
                GET /api/companies/active - 활성 회사만 조회

                🔎 검색:
                GET /api/companies/search?keyword=키워드 - 회사명 검색
                GET /api/companies/search-ignore-case?name=검색어 - 대소문자 무관 검색

                📈 통계:
                GET /api/companies/count - 전체 개수
                GET /api/companies/count/status/{status} - 상태별 개수
                GET /api/companies/exists?name=회사명 - 존재 여부 확인

                💡 팁: 브라우저나 curl 명령어로 테스트 가능합니다!
                """;
    }
}