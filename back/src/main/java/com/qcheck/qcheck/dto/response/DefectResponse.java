package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 불량 조회 응답 DTO
 *
 * 목적:
 * - GET /api/defects (목록) 및 GET /api/defects/{seq} (상세)에서 반환
 * - 프론트엔드에서 불량 정보 표시
 *
 * 필드:
 * - defectSeq: 불량 ID
 * - occurDate: 불량 발생 일자
 * - unitNm: 공정 단위명
 * - lineNm: 생산 라인명
 * - partNm: 부품명
 * - codeNm: 불량 코드명
 * - position: 불량 위치
 * - quantity: 불량 수량
 * - remarks: 비고
 * - hasImage: 이미지 첨부 여부
 * - createdAt: 생성 일시
 * - createdEp: 생성자
 * - updatedAt: 수정 일시
 * - updatedEp: 수정자
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectResponse {

    /**
     * 불량 ID
     */
    private Long defectSeq;

    /**
     * 불량 발생 일자
     * 포맷: yyyy-MM-dd
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate occurDate;

    /**
     * 공정 단위명
     * 예: P1, P2 등
     */
    private String unitNm;

    /**
     * 생산 라인명
     * 예: Line A, Line B 등
     */
    private String lineNm;

    /**
     * 부품명
     * 예: PartA, CPU 등
     */
    private String partNm;

    /**
     * 불량 코드명
     * 예: Scratch, Dent 등
     */
    private String codeNm;

    /**
     * 불량 위치
     * 예: A1, B2 등
     */
    private String position;

    /**
     * 불량 수량
     */
    private Integer quantity;

    /**
     * 비고
     */
    private String remarks;

    /**
     * 이미지 첨부 여부
     * true: 이미지 있음, false: 없음
     */
    private Boolean hasImage;

    /**
     * 생성 일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 생성자
     */
    private String createdEp;

    /**
     * 수정 일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 수정자
     */
    private String updatedEp;
}
