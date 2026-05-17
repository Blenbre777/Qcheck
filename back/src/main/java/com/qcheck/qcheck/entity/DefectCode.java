package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 불량 코드 마스터 엔티티
 *
 * 책임:
 * - 발생 가능한 불량 유형의 마스터 코드 저장
 * - 회사별 불량 코드 관리
 *
 * 컬럼:
 * - defect_code_seq: BIGSERIAL PK
 * - company_seq: BIGINT FK (회사 ID, 다중 테넌트 격리)
 * - code_nm: VARCHAR(50) 불량 코드명 (예: DEFNO1, DEFNO2)
 * - description: VARCHAR(255) 불량 설명
 * - use_yn: VARCHAR(2) 사용 여부 (Y/N, soft delete)
 * - Audit: created_at, created_ep, updated_at, updated_ep
 *
 * 제약 조건:
 * - UNIQUE(company_seq, code_nm) - 같은 회사 내 코드는 중복 불가
 *
 * 특징:
 * - Soft Delete: use_yn 플래그로 논리적 삭제 구현
 * - Multi-Tenant: company_seq로 회사 간 격리
 * - 불량 등록 시 드롭다운에서 선택
 * - 대시보드 Top3 불량 코드 집계에 사용
 */
@Entity
@Table(
    name = "defect_code",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_seq", "code_nm"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectCode {

    /**
     * 불량 코드 ID (BIGSERIAL PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defect_code_seq")
    private Long defectCodeSeq;

    /**
     * 소속 회사 ID (FK)
     * 다중 테넌트 격리를 위한 필수 컬럼
     */
    @Column(name = "company_seq", nullable = false)
    private Long companySeq;

    /**
     * 불량 코드명
     * 예: DEFNO1, DEFNO2, WRONG_SIZE, SCRATCH 등
     */
    @Column(name = "code_nm", nullable = false, length = 50)
    private String codeNm;

    /**
     * 불량 설명
     * 선택사항이지만 사용자 편의를 위해 제공
     * 예: "화학적 변색", "기계적 손상", "치수 오류" 등
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 사용 여부
     * Y: 활성화, N: 비활성화 (soft delete)
     * 드롭다운 조회 시 Y인 항목만 반환
     */
    @Column(name = "use_yn", nullable = false, length = 2)
    private String useYn;

    /**
     * 생성 일시
     */
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * 생성자
     */
    @Column(name = "created_ep", nullable = false, length = 50)
    private String createdEp;

    /**
     * 수정 일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 수정자
     */
    @Column(name = "updated_ep", length = 50)
    private String updatedEp;

    /**
     * 엔티티 생성 전 처리
     * 생성 일시와 사용 여부를 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.useYn == null) {
            this.useYn = "Y";
        }
    }

    /**
     * 엔티티 수정 전 처리
     * 수정 일시를 자동 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
