package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대시보드 통계 응답 DTO
 *
 * 용도:
 * - 대시보드 페이지에서 표시할 모든 통계 정보 제공
 * - 불량 요약 통계 (총 건수, 고유 위치 수)
 * - 라인별 불량 수 분포
 * - 불량 코드별 불량 수 분포
 *
 * 필드:
 * - totalDefectCount: 불량 총 건수
 * - uniquePositionCount: 고유 위치 개수
 * - defectCountByLine: 라인별 불량 수 리스트
 * - defectCountByCode: 불량 코드별 불량 수 리스트
 *
 * 참고:
 * - 모든 통계는 회사별 격리 (companySeq에서 추출)
 * - 날짜 범위: fromDate ~ toDate 기간의 데이터만 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    /**
     * 불량 총 건수
     */
    @JsonProperty("total_defect_count")
    private Long totalDefectCount;

    /**
     * 고유 위치 개수 (position 필드의 distinct count)
     */
    @JsonProperty("unique_position_count")
    private Long uniquePositionCount;

    /**
     * 라인별 불량 수 집계 (내림차순 정렬)
     */
    @JsonProperty("defect_count_by_line")
    private List<DefectCountByLineResponse> defectCountByLine;

    /**
     * 불량 코드별 불량 수 집계 (내림차순 정렬)
     */
    @JsonProperty("defect_count_by_code")
    private List<DefectCountByCodeResponse> defectCountByCode;
}
