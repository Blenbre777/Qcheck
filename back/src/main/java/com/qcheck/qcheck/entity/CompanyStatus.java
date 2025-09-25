package com.qcheck.qcheck.entity;

/**
 * 고객사 상태를 나타내는 열거형
 *
 * 목적: 고객사의 현재 운영 상태를 관리하기 위한 상수 정의
 * 사용처: Company 엔터티의 status 필드에서 사용
 *
 * 상태 종류:
 * - ACTIVE: 활성 상태 (정상 서비스 이용 가능)
 * - INACTIVE: 비활성 상태 (서비스 이용 중단)
 * - SUSPENDED: 일시중단 상태 (임시 서비스 중단)
 *
 * 데이터베이스 저장: 문자열 형태로 저장 (예: "ACTIVE", "INACTIVE", "SUSPENDED")
 * JPA 매핑: @Enumerated(EnumType.STRING) 사용 권장
 */
public enum CompanyStatus {
    /**
     * 활성 상태
     * - 정상적으로 서비스를 이용할 수 있는 상태
     * - 모든 기능 접근 가능
     */
    ACTIVE,

    /**
     * 비활성 상태
     * - 서비스 이용이 중단된 상태
     * - 로그인 및 기능 접근 제한
     */
    INACTIVE,

    /**
     * 일시중단 상태
     * - 임시적으로 서비스가 중단된 상태
     * - 관리자 판단에 의한 일시적 제한
     */
    SUSPENDED
}