package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 불량 레코드 리포지토리
 *
 * 책임:
 * - 불량 데이터베이스 접근 계층
 * - 불량 검색 및 필터링 쿼리 정의
 * - 페이지네이션 지원
 *
 * 특징:
 * - 날짜 범위 필터: findByCompanySeqAndOccurDateBetween
 * - 키워드 검색: JPQL로 여러 필드 검색 (position, remarks)
 * - 회사별 격리: 모든 쿼리에서 companySeq 필터링
 */
@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {

    /**
     * 회사별 + 날짜 범위로 불량 검색
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param pageable 페이지 정보
     * @return 불량 페이지 결과
     */
    Page<Defect> findByCompanySeqAndOccurDateBetween(
        Long companySeq,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );

    /**
     * 회사별 + 날짜 범위 + 키워드로 불량 검색
     *
     * 검색 대상:
     * - position: 불량 위치 (부분 일치, 대소문자 무시)
     * - remarks: 비고 (부분 일치, 대소문자 무시)
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 불량 페이지 결과
     */
    @Query("""
        SELECT d FROM Defect d
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        AND (LOWER(d.position) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(d.remarks) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Defect> findByCompanySeqAndOccurDateBetweenAndKeyword(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * 불량 ID와 회사 ID로 단일 불량 조회
     *
     * @param defectSeq 불량 ID
     * @param companySeq 회사 ID
     * @return 불량 정보 (Optional)
     */
    Optional<Defect> findByDefectSeqAndCompanySeq(Long defectSeq, Long companySeq);

    /**
     * 회사별 + 라인별 불량 수 집계 (대시보드용)
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 불량 총 건수
     */
    @Query("""
        SELECT COUNT(d) FROM Defect d
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        """)
    Long countByCompanySeqAndOccurDateBetween(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * 회사별 고유 위치 개수 집계 (대시보드용)
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 고유 위치 개수
     */
    @Query("""
        SELECT COUNT(DISTINCT d.position) FROM Defect d
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        AND d.position IS NOT NULL
        """)
    Long countDistinctPositionByCompanySeqAndOccurDateBetween(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
