package com.qcheck.qcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 불량 페이지 응답 DTO
 *
 * 목적:
 * - GET /api/defects (목록 조회) 응답에 페이지네이션 정보 포함
 *
 * 필드:
 * - content: 불량 데이터 목록
 * - totalElements: 전체 레코드 수
 * - totalPages: 전체 페이지 수
 * - currentPage: 현재 페이지 번호 (0-indexed)
 * - pageSize: 페이지당 레코드 수
 * - hasNext: 다음 페이지 존재 여부
 * - hasPrevious: 이전 페이지 존재 여부
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedDefectResponse {

    /**
     * 불량 데이터 목록
     */
    private List<DefectResponse> content;

    /**
     * 전체 레코드 수
     */
    private Long totalElements;

    /**
     * 전체 페이지 수
     */
    private Integer totalPages;

    /**
     * 현재 페이지 번호 (0-indexed)
     */
    private Integer currentPage;

    /**
     * 페이지당 레코드 수
     */
    private Integer pageSize;

    /**
     * 다음 페이지 존재 여부
     */
    private Boolean hasNext;

    /**
     * 이전 페이지 존재 여부
     */
    private Boolean hasPrevious;

    /**
     * Spring Data Page 객체로부터 PagedDefectResponse 생성
     *
     * @param page Spring Data Page 객체 (제네릭 와일드카드 - Page<Defect>도 가능)
     * @param content DefectResponse 리스트
     * @return PagedDefectResponse
     */
    public static PagedDefectResponse of(Page<?> page, List<DefectResponse> content) {
        return PagedDefectResponse.builder()
            .content(content)
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
