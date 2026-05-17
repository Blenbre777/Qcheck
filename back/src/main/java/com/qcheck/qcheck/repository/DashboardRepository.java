package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.dto.response.DefectCountByLineResponse;
import com.qcheck.qcheck.dto.response.DefectCountByCodeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.qcheck.qcheck.entity.Defect;

import java.time.LocalDate;
import java.util.List;

/**
 * 대시보드 리포지토리
 *
 * 책임:
 * - 대시보드 통계 데이터 조회
 * - 불량 집계 (라인별, 불량 코드별)
 * - 회사별 격리된 통계 조회
 *
 * 특징:
 * - 라인별 불량 수 집계: Defect 엔티티 조인 및 GROUP BY
 * - 불량 코드별 불량 수 집계: Defect + DefectCode 조인
 * - 모든 쿼리에서 companySeq 필터링 (다중 테넌트 격리)
 */
@Repository
public interface DashboardRepository extends JpaRepository<Defect, Long> {

    /**
     * 회사별 라인별 불량 수 집계
     *
     * 쿼리 결과:
     * - line_seq: 라인 ID
     * - line_nm: 라인명
     * - defect_count: 불량 수
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 라인별 불량 수 리스트
     */
    @Query("""
        SELECT new com.qcheck.qcheck.dto.response.DefectCountByLineResponse(
            l.lineSeq, l.lineNm, COUNT(d) as defectCount
        )
        FROM Defect d
        JOIN Line l ON d.lineSeq = l.lineSeq
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        GROUP BY l.lineSeq, l.lineNm
        ORDER BY COUNT(d) DESC
        """)
    List<DefectCountByLineResponse> findDefectCountByLineAndCompany(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * 회사별 불량 코드별 불량 수 집계
     *
     * 쿼리 결과:
     * - defect_code_seq: 불량 코드 ID
     * - defect_code_nm: 불량 코드명
     * - defect_count: 불량 수
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 불량 코드별 불량 수 리스트
     */
    @Query("""
        SELECT new com.qcheck.qcheck.dto.response.DefectCountByCodeResponse(
            dc.defectCodeSeq, dc.codeNm, COUNT(d) as defectCount
        )
        FROM Defect d
        JOIN DefectCode dc ON d.defectCodeSeq = dc.defectCodeSeq
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        GROUP BY dc.defectCodeSeq, dc.codeNm
        ORDER BY COUNT(d) DESC
        """)
    List<DefectCountByCodeResponse> findDefectCountByCodeAndCompany(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * 회사별 라인별 불량 수 집계 (상위 N개)
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param limit 조회할 최대 개수
     * @return 상위 N개 라인별 불량 수 리스트
     */
    @Query(value = """
        SELECT new com.qcheck.qcheck.dto.response.DefectCountByLineResponse(
            l.lineSeq, l.lineNm, COUNT(d) as defectCount
        )
        FROM Defect d
        JOIN Line l ON d.lineSeq = l.lineSeq
        WHERE d.companySeq = :companySeq
        AND d.occurDate BETWEEN :fromDate AND :toDate
        GROUP BY l.lineSeq, l.lineNm
        ORDER BY COUNT(d) DESC
        """)
    List<DefectCountByLineResponse> findTopDefectCountByLine(
        @Param("companySeq") Long companySeq,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
