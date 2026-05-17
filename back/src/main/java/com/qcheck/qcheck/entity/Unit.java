package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공정 단위 엔티티
 *
 * 책임:
 * - 생산 라인 내 세부 공정 단위 정보 저장
 * - 불량 발생 위치를 더 상세하게 기록하기 위한 마스터 데이터
 *
 * 컬럼:
 * - unit_seq: BIGSERIAL PK
 * - company_seq: BIGINT FK (회사 ID, 다중 테넌트 격리)
 * - unit_nm: VARCHAR(100) 공정 단위명
 * - use_yn: VARCHAR(2) 사용 여부 (Y/N, soft delete)
 * - Audit: created_at, created_ep, updated_at, updated_ep
 *
 * 제약 조건:
 * - UNIQUE(company_seq, unit_nm) - 같은 회사 내 공정 단위명은 중복 불가
 *
 * 특징:
 * - Soft Delete: use_yn 플래그로 논리적 삭제 구현
 * - Multi-Tenant: company_seq로 회사 간 격리
 * - 목업 및 디자인에서 P1, P2 등으로 표현 (공정 단계)
 * - 불량 등록 시 라인, 부품과 함께 드롭다운에서 선택
 */
@Entity
@Table(
    name = "unit",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_seq", "unit_nm"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    /**
     * 공정 단위 ID (BIGSERIAL PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_seq")
    private Long unitSeq;

    /**
     * 소속 회사 ID (FK)
     * 다중 테넌트 격리를 위한 필수 컬럼
     */
    @Column(name = "company_seq", nullable = false)
    private Long companySeq;

    /**
     * 공정 단위명
     * 예: P1, P2, Assembly, Welding 등
     * 목업에서 P1, P2로 표현된 생산 공정 단계
     */
    @Column(name = "unit_nm", nullable = false, length = 100)
    private String unitNm;

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
