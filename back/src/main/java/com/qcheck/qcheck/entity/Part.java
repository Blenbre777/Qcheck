package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 부품 마스터 엔티티
 *
 * 책임:
 * - 제조 시설에서 생산되는 부품/제품 정보 저장
 * - 회사별 부품 마스터 데이터 관리
 *
 * 컬럼:
 * - part_seq: BIGSERIAL PK
 * - company_seq: BIGINT FK (회사 ID, 다중 테넌트 격리)
 * - part_nm: VARCHAR(100) 부품명
 * - use_yn: VARCHAR(2) 사용 여부 (Y/N, soft delete)
 * - Audit: created_at, created_ep, updated_at, updated_ep
 *
 * 제약 조건:
 * - UNIQUE(company_seq, part_nm) - 같은 회사 내 부품명은 중복 불가
 *
 * 특징:
 * - Soft Delete: use_yn 플래그로 논리적 삭제 구현
 * - Multi-Tenant: company_seq로 회사 간 격리
 * - 불량 등록 시 라인과 함께 드롭다운에서 선택
 */
@Entity
@Table(
    name = "part",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_seq", "part_nm"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Part {

    /**
     * 부품 ID (BIGSERIAL PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_seq")
    private Long partSeq;

    /**
     * 소속 회사 ID (FK)
     * 다중 테넌트 격리를 위한 필수 컬럼
     */
    @Column(name = "company_seq", nullable = false)
    private Long companySeq;

    /**
     * 부품명
     * 예: PartA, PartB, CPU 등
     */
    @Column(name = "part_nm", nullable = false, length = 100)
    private String partNm;

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
