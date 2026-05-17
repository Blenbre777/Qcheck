package com.qcheck.qcheck.entity;

/**
 * 사원 권한을 나타내는 열거형
 *
 * 목적: 사원의 시스템 권한 수준을 관리하기 위한 상수 정의
 * 사용처: Emp 엔티티의 empRole 필드, JWT 토큰의 권한 정보
 *
 * 권한 종류:
 * - SUPER_ADMIN: 시스템 전체 관리자 (모든 회사, 모든 기능 접근)
 * - COMPANY_ADMIN: 회사 관리자 (소속 회사의 관리자 기능)
 * - USER: 일반 사용자 (기본 조회/작성 기능만)
 *
 * 데이터베이스 저장: 문자열 형태로 저장 (예: "SUPER_ADMIN", "COMPANY_ADMIN", "USER")
 * JPA 매핑: @Enumerated(EnumType.STRING) 사용
 */
public enum EmpRole {
    /**
     * 시스템 관리자
     * - 모든 회사의 데이터에 접근 가능
     * - 회사 생성/삭제 가능
     * - 사용자 권한 관리 가능
     */
    SUPER_ADMIN("시스템 관리자"),

    /**
     * 회사 관리자
     * - 소속 회사의 모든 기능 관리 가능
     * - 회사의 마스터 데이터 관리
     * - 회사의 사용자 관리
     */
    COMPANY_ADMIN("회사 관리자"),

    /**
     * 일반 사용자
     * - 소속 회사의 불량 데이터 조회/등록
     * - 대시보드 조회
     * - 마스터 데이터 조회 (읽기 전용)
     */
    USER("일반 사용자");

    /**
     * 권한의 한글 설명
     */
    private final String description;

    EmpRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 권한 문자열로 EmpRole 조회
     * @param value 권한 문자열 (예: "USER", "COMPANY_ADMIN")
     * @return EmpRole 열거값, 없으면 null
     */
    public static EmpRole fromString(String value) {
        try {
            return EmpRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
