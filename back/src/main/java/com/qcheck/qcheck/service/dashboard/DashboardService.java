package com.qcheck.qcheck.service.dashboard;

import com.qcheck.qcheck.dto.response.DashboardResponse;
import com.qcheck.qcheck.dto.response.DefectCountByCodeResponse;
import com.qcheck.qcheck.dto.response.DefectCountByLineResponse;
import com.qcheck.qcheck.repository.DefectRepository;
import com.qcheck.qcheck.repository.DashboardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 대시보드 서비스
 *
 * 책임:
 * - 대시보드 통계 데이터 조회 및 집계
 * - 불량 요약 통계 (총 건수, 고유 위치 수)
 * - 라인별, 불량 코드별 불량 수 분포
 * - 회사별 격리 처리 (companySeq 필터링)
 *
 * 특징:
 * - 읽기 전용 트랜잭션 (@Transactional(readOnly = true))
 * - 다중 리포지토리 활용 (DashboardRepository, DefectRepository)
 * - 효율적인 집계 쿼리 (GROUP BY, COUNT)
 * - 응답 생성 시 null-safe 처리
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private DefectRepository defectRepository;

    /**
     * 대시보드 통계 조회
     *
     * 처리:
     * 1. 불량 총 건수 조회
     * 2. 고유 위치 개수 조회
     * 3. 라인별 불량 수 집계 (상위 N개)
     * 4. 불량 코드별 불량 수 집계 (상위 N개)
     * 5. 응답 객체 생성 및 반환
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 대시보드 통계 응답
     */
    public DashboardResponse getDashboardStats(Long companySeq, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching dashboard stats for company {} from {} to {}", companySeq, fromDate, toDate);

        // 1. 불량 총 건수
        Long totalDefectCount = defectRepository.countByCompanySeqAndOccurDateBetween(
            companySeq, fromDate, toDate
        );

        // 2. 고유 위치 개수
        Long uniquePositionCount = defectRepository.countDistinctPositionByCompanySeqAndOccurDateBetween(
            companySeq, fromDate, toDate
        );

        // 3. 라인별 불량 수 집계
        List<DefectCountByLineResponse> defectCountByLine = dashboardRepository.findDefectCountByLineAndCompany(
            companySeq, fromDate, toDate
        );

        // 4. 불량 코드별 불량 수 집계
        List<DefectCountByCodeResponse> defectCountByCode = dashboardRepository.findDefectCountByCodeAndCompany(
            companySeq, fromDate, toDate
        );

        // 5. 응답 객체 생성
        DashboardResponse response = DashboardResponse.builder()
            .totalDefectCount(totalDefectCount != null ? totalDefectCount : 0L)
            .uniquePositionCount(uniquePositionCount != null ? uniquePositionCount : 0L)
            .defectCountByLine(defectCountByLine != null ? defectCountByLine : List.of())
            .defectCountByCode(defectCountByCode != null ? defectCountByCode : List.of())
            .build();

        log.info("Dashboard stats retrieved: total={}, uniquePositions={}, lines={}, codes={}",
            response.getTotalDefectCount(),
            response.getUniquePositionCount(),
            response.getDefectCountByLine().size(),
            response.getDefectCountByCode().size()
        );

        return response;
    }

    /**
     * 라인별 불량 수 조회
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 라인별 불량 수 리스트 (내림차순)
     */
    public List<DefectCountByLineResponse> getDefectCountByLine(Long companySeq, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching defect count by line for company {}", companySeq);
        return dashboardRepository.findDefectCountByLineAndCompany(companySeq, fromDate, toDate);
    }

    /**
     * 불량 코드별 불량 수 조회
     *
     * @param companySeq 회사 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 불량 코드별 불량 수 리스트 (내림차순)
     */
    public List<DefectCountByCodeResponse> getDefectCountByCode(Long companySeq, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching defect count by code for company {}", companySeq);
        return dashboardRepository.findDefectCountByCodeAndCompany(companySeq, fromDate, toDate);
    }
}
