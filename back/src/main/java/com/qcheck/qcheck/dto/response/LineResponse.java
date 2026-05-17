package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 라인 응답 DTO
 *
 * 용도:
 * - GET /api/master/lines: 라인 목록 조회
 * - GET /api/master/lines/{lineSeq}: 라인 상세 조회
 * - POST /api/master/lines: 라인 생성
 * - PUT /api/master/lines/{lineSeq}: 라인 수정
 *
 * 필드:
 * - lineSeq: 라인 ID
 * - lineNm: 라인명
 * - useYn: 사용 여부
 * - createdAt: 생성 일시
 * - createdEp: 생성자
 * - updatedAt: 수정 일시 (null 가능)
 * - updatedEp: 수정자 (null 가능)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineResponse {

    /**
     * 라인 ID
     */
    private Long lineSeq;

    /**
     * 라인명
     */
    private String lineNm;

    /**
     * 사용 여부 (Y/N)
     */
    private String useYn;

    /**
     * 생성 일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 생성자
     */
    private String createdEp;

    /**
     * 수정 일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 수정자
     */
    private String updatedEp;
}
