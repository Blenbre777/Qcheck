package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 라인별 불량 수 집계 응답 DTO
 *
 * 용도:
 * - 대시보드 라인별 불량 수 조회
 * - 차트 데이터 제공 (X축: 라인명, Y축: 불량 수)
 *
 * 필드:
 * - lineSeq: 라인 ID
 * - lineNm: 라인명
 * - defectCount: 불량 수
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectCountByLineResponse {

    /**
     * 라인 ID
     */
    @JsonProperty("line_seq")
    private Long lineSeq;

    /**
     * 라인명
     */
    @JsonProperty("line_nm")
    private String lineNm;

    /**
     * 불량 수
     */
    @JsonProperty("defect_count")
    private Long defectCount;
}
