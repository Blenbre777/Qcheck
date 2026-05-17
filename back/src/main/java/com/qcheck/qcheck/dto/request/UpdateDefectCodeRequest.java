package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 불량 코드 수정 요청 DTO
 *
 * 요청 본문: PUT /api/master/defect-codes/{defectCodeSeq}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDefectCodeRequest {

    @NotBlank(message = "불량 코드명은 필수입니다")
    private String codeNm;

    private String description;

    private String useYn;
}
