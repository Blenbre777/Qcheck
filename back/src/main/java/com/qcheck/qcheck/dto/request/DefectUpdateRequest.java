package com.qcheck.qcheck.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 불량 수정 요청 DTO
 *
 * 목적:
 * - PUT /api/defects/{defect_seq}로 기존 불량 레코드 수정
 * - multipart/form-data로 텍스트 필드 + 파일 첨부 지원
 *
 * 필드:
 * - lineSeq: 생산 라인 ID
 * - partSeq: 부품 ID
 * - unitSeq: 공정 단위 ID
 * - defectCodeSeq: 불량 코드 ID
 * - quantity: 불량 수량
 * - position: 불량 위치 (선택)
 * - remarks: 비고 (선택)
 * - occurDate: 불량 발생 일자 (필수)
 *
 * 파일 첨부:
 * - 이미지 파일은 DefectController에서 @RequestPart("file")로 처리
 * - null이면 기존 이미지 유지
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectUpdateRequest {

    /**
     * 생산 라인 ID (필수)
     */
    @NotNull(message = "생산 라인을 선택해주세요")
    private Long lineSeq;

    /**
     * 부품 ID (필수)
     */
    @NotNull(message = "부품을 선택해주세요")
    private Long partSeq;

    /**
     * 공정 단위 ID (필수)
     */
    @NotNull(message = "공정 단위를 선택해주세요")
    private Long unitSeq;

    /**
     * 불량 코드 ID (필수)
     */
    @NotNull(message = "불량 코드를 선택해주세요")
    private Long defectCodeSeq;

    /**
     * 불량 수량 (최소: 1)
     */
    @Positive(message = "불량 수량은 1 이상이어야 합니다")
    private Integer quantity;

    /**
     * 불량 위치 (선택, 최대 100자)
     * 예: A1, B2 등
     */
    @Size(max = 100, message = "불량 위치는 100자 이하여야 합니다")
    private String position;

    /**
     * 비고 (선택)
     */
    private String remarks;

    /**
     * 불량 발생 일자 (필수)
     * 포맷: yyyy-MM-dd
     */
    @NotNull(message = "불량 발생 일자를 선택해주세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate occurDate;
}
