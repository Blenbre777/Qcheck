package com.qcheck.qcheck.controller;

import com.qcheck.qcheck.dto.response.DashboardResponse;
import com.qcheck.qcheck.dto.response.DefectCountByCodeResponse;
import com.qcheck.qcheck.dto.response.DefectCountByLineResponse;
import com.qcheck.qcheck.service.dashboard.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 대시보드 REST API 컨트롤러
 *
 * 책임:
 * - 대시보드 통계 데이터 조회 HTTP 엔드포인트 제공
 * - JWT에서 companySeq 추출 (회사 격리)
 * - 요청 파라미터 검증 및 서비스 호출
 * - 응답 형식화
 *
 * 엔드포인트:
 * - GET /api/dashboard: 대시보드 전체 통계 조회 (요약 + 라인별 + 코드별)
 * - GET /api/dashboard/line: 라인별 불량 수 조회
 * - GET /api/dashboard/code: 불량 코드별 불량 수 조회
 *
 * 쿼리 파라미터:
 * - from: 시작 날짜 (필수, yyyy-MM-dd 형식)
 * - to: 종료 날짜 (필수, yyyy-MM-dd 형식)
 *
 * 회사 격리:
 * - JWT의 companySeq claim에서 자동 추출
 * - 모든 작업에 강제 적용
 *
 * 응답:
 * - 200 OK: 통계 데이터 반환
 * - 400 Bad Request: 파라미터 검증 실패
 * - 401 Unauthorized: 인증 실패
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 대시보드 전체 통계 조회
     *
     * 반환 데이터:
     * - totalDefectCount: 불량 총 건수
     * - uniquePositionCount: 고유 위치 개수
     * - defectCountByLine: 라인별 불량 수 (상위 순서)
     * - defectCountByCode: 불량 코드별 불량 수 (상위 순서)
     *
     * 예시 요청:
     * GET /api/dashboard?from=2025-01-01&to=2025-01-31
     *
     * 예시 응답:
     * {
     *   "total_defect_count": 150,
     *   "unique_position_count": 45,
     *   "defect_count_by_line": [
     *     {"line_seq": 1, "line_nm": "Line A", "defect_count": 50},
     *     {"line_seq": 2, "line_nm": "Line B", "defect_count": 45}
     *   ],
     *   "defect_count_by_code": [
     *     {"defect_code_seq": 1, "code_nm": "색상", "defect_count": 80},
     *     {"defect_code_seq": 2, "code_nm": "흠집", "defect_count": 70}
     *   ]
     * }
     *
     * @param from 시작 날짜 (yyyy-MM-dd)
     * @param to 종료 날짜 (yyyy-MM-dd)
     * @param httpRequest HTTP 요청 (companySeq 추출용)
     * @param authentication 인증 정보
     * @return 대시보드 통계 응답
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardStats(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
        HttpServletRequest httpRequest,
        Authentication authentication
    ) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        log.info("Dashboard stats requested for company {} from {} to {}", companySeq, from, to);

        DashboardResponse response = dashboardService.getDashboardStats(companySeq, from, to);
        return ResponseEntity.ok(response);
    }

    /**
     * 라인별 불량 수 조회
     *
     * 반환 데이터:
     * - 라인별 불량 수를 내림차순으로 정렬한 리스트
     *
     * 예시 요청:
     * GET /api/dashboard/line?from=2025-01-01&to=2025-01-31
     *
     * @param from 시작 날짜 (yyyy-MM-dd)
     * @param to 종료 날짜 (yyyy-MM-dd)
     * @param httpRequest HTTP 요청 (companySeq 추출용)
     * @param authentication 인증 정보
     * @return 라인별 불량 수 리스트
     */
    @GetMapping("/line")
    public ResponseEntity<List<DefectCountByLineResponse>> getDefectCountByLine(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
        HttpServletRequest httpRequest,
        Authentication authentication
    ) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        log.info("Defect count by line requested for company {} from {} to {}", companySeq, from, to);

        List<DefectCountByLineResponse> response = dashboardService.getDefectCountByLine(companySeq, from, to);
        return ResponseEntity.ok(response);
    }

    /**
     * 불량 코드별 불량 수 조회
     *
     * 반환 데이터:
     * - 불량 코드별 불량 수를 내림차순으로 정렬한 리스트
     *
     * 예시 요청:
     * GET /api/dashboard/code?from=2025-01-01&to=2025-01-31
     *
     * @param from 시작 날짜 (yyyy-MM-dd)
     * @param to 종료 날짜 (yyyy-MM-dd)
     * @param httpRequest HTTP 요청 (companySeq 추출용)
     * @param authentication 인증 정보
     * @return 불량 코드별 불량 수 리스트
     */
    @GetMapping("/code")
    public ResponseEntity<List<DefectCountByCodeResponse>> getDefectCountByCode(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
        HttpServletRequest httpRequest,
        Authentication authentication
    ) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        log.info("Defect count by code requested for company {} from {} to {}", companySeq, from, to);

        List<DefectCountByCodeResponse> response = dashboardService.getDefectCountByCode(companySeq, from, to);
        return ResponseEntity.ok(response);
    }

    // ===== HELPER METHODS =====

    /**
     * HttpServletRequest에서 companySeq 추출
     * JwtAuthenticationFilter에서 요청 속성에 저장된 companySeq를 반환
     *
     * @param request HTTP 요청
     * @return companySeq
     * @throws RuntimeException companySeq를 찾을 수 없으면 예외 발생
     */
    private Long getCompanySeqFromRequest(HttpServletRequest request) {
        try {
            Object companySeqAttr = request.getAttribute("companySeq");
            if (companySeqAttr instanceof Long) {
                return (Long) companySeqAttr;
            }
            throw new RuntimeException("회사 정보를 찾을 수 없습니다");
        } catch (Exception e) {
            throw new RuntimeException("회사 정보 추출 중 오류 발생: " + e.getMessage());
        }
    }
}
