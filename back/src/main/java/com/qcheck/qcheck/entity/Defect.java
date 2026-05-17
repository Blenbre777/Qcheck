package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 불량 레코드 엔티티
 *
 * 책임:
 * - 생산 라인에서 발생한 불량 정보 저장
 * - 회사별 불량 이력 관리
 *
 * 컬럼:
 * - defect_seq: BIGSERIAL PK
 * - company_seq: BIGINT FK (회사 ID, 다중 테넌트 격리)
 * - line_seq: BIGINT FK (라인 ID)
 * - part_seq: BIGINT FK (부품 ID)
 * - unit_seq: BIGINT FK (공정 단위 ID)
 * - defect_code_seq: BIGINT FK (불량 코드 ID)
 * - quantity: INT (불량 수량, DEFAULT 1)
 * - position: VARCHAR(100) (불량 위치)
 * - remarks: TEXT (비고)
 * - occur_date: DATE (불량 발생 일자, NOT NULL)
 * - image_path: VARCHAR(500) (이미지 파일 경로)
 * - Audit: created_at, created_ep, updated_at, updated_ep
 *
 * 특징:
 * - Multi-Tenant: company_seq로 회사 간 격리
 * - 이미지 첨부: image_path로 파일 경로 저장
 * - 불량 등록 시 라인, 부품, 공정, 불량코드 필수
 */
@Entity
@Table(name = "defect")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defect {

    /**
     * 불량 ID (BIGSERIAL PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defect_seq")
    private Long defectSeq;

    /**
     * 소속 회사 ID (FK)
     * 다중 테넌트 격리를 위한 필수 컬럼
     */
    @Column(name = "company_seq", nullable = false)
    private Long companySeq;

    /**
     * 생산 라인 ID (FK)
     */
    @Column(name = "line_seq", nullable = false)
    private Long lineSeq;

    /**
     * 부품 ID (FK)
     */
    @Column(name = "part_seq", nullable = false)
    private Long partSeq;

    /**
     * 공정 단위 ID (FK)
     */
    @Column(name = "unit_seq", nullable = false)
    private Long unitSeq;

    /**
     * 불량 코드 ID (FK)
     */
    @Column(name = "defect_code_seq", nullable = false)
    private Long defectCodeSeq;

    /**
     * 불량 수량
     * 기본값: 1
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 불량 위치
     * 예: A1, B2 등 위치 정보
     */
    @Column(name = "position", length = 100)
    private String position;

    /**
     * 비고/메모
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * 불량 발생 일자
     * 필수 입력 항목
     */
    @Column(name = "occur_date", nullable = false)
    private LocalDate occurDate;

    /**
     * 이미지 파일 경로
     * 예: /files/defects/1/2026-05/abc123.jpg
     */
    @Column(name = "image_path", length = 500)
    private String imagePath;

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
     * 생성 일시를 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
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
