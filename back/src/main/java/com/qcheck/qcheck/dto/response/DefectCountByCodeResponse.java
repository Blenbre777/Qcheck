package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 불량 코드별 불량 수 집계 응답 DTO
 *
 * 용도:
 * - 대시보드 불량 코드별 불량 수 조회
 * - 차트 데이터 제공 (X축: 불량 코드명, Y축: 불량 수)
 *
 * 필드:
 * - defectCodeSeq: 불량 코드 ID
 * - codeNm: 불량 코드명
 * - defectCount: 불량 수
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectCountByCodeResponse {

    /**
     * 불량 코드 ID
     */
    @JsonProperty("defect_code_seq")
    private Long defectCodeSeq;

    /**
     * 불량 코드명
     */
    @JsonProperty("code_nm")
    private String codeNm;

    /**
     * 불량 수
     */
    @JsonProperty("defect_count")
    private Long defectCount;
}
