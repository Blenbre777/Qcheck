package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 불량 코드 응답 DTO
 *
 * 용도: GET /api/master/defect-codes, POST/PUT /api/master/defect-codes/{defectCodeSeq}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectCodeResponse {

    private Long defectCodeSeq;

    private String codeNm;

    private String description;

    private String useYn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdEp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String updatedEp;
}
