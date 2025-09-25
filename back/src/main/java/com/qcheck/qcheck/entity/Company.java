package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 고객사 엔터티 클래스
 *
 * 목적: 고객사 정보를 데이터베이스의 company 테이블과 매핑
 * 테이블명: company
 * 주요 기능: 고객사의 기본 정보와 상태 관리
 *
 * 필드 구성:
 * - id: 고유 식별자 (기본키, 자동 증가)
 * - name: 고객사명 (필수, 최대 100자)
 * - status: 고객사 상태 (ACTIVE, INACTIVE, SUSPENDED)
 *
 * JPA 어노테이션 설명:
 * - @Entity: JPA 엔터티임을 선언
 * - @Table: 데이터베이스 테이블과 매핑
 * - @Id: 기본키 지정
 * - @GeneratedValue: 자동 증가 값 생성
 * - @Column: 데이터베이스 컬럼과 매핑 및 제약조건
 * - @Enumerated: Enum 타입의 데이터베이스 저장 방식 지정
 *
 * Lombok 어노테이션 설명:
 * - @Entity: 엔터티 클래스 표시
 * - @Table: 테이블 매핑 정보
 * - @Getter/@Setter: getter/setter 메서드 자동 생성
 * - @Builder: 빌더 패턴 자동 생성
 * - @NoArgsConstructor: 기본 생성자 자동 생성
 * - @AllArgsConstructor: 모든 필드 매개변수 생성자 자동 생성
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    /**
     * 고객사 고유 식별자
     *
     * 자료형: Long
     * 데이터베이스: BIGSERIAL (PostgreSQL 자동 증가)
     * 제약조건: PRIMARY KEY, NOT NULL
     *
     * JPA 설정:
     * - @Id: 기본키 지정
     * - @GeneratedValue: 자동 증가 값 생성
     * - strategy = GenerationType.IDENTITY: 데이터베이스의 IDENTITY 컬럼 사용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 고객사명
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(100)
     * 제약조건: NOT NULL
     *
     * 사용 목적:
     * - 고객사의 공식 명칭 저장
     * - 관리자 페이지에서 고객사 식별
     * - 로그인 시 소속 회사 표시
     *
     * 유효성 검증: 필수 입력, 최대 100자
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 고객사 상태
     *
     * 자료형: CompanyStatus (enum)
     * 데이터베이스: VARCHAR(20)
     * 제약조건: NOT NULL
     *
     * 가능한 값:
     * - ACTIVE: 활성 상태
     * - INACTIVE: 비활성 상태
     * - SUSPENDED: 일시중단 상태
     *
     * JPA 설정:
     * - @Enumerated(EnumType.STRING): enum을 문자열로 저장
     * - 숫자 대신 문자열 저장으로 가독성 향상
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CompanyStatus status;

    /**
     * 객체의 문자열 표현 반환
     *
     * 반환값: String
     * 포함 정보: 클래스명, id, name, status
     *
     * 사용 목적:
     * - 디버깅 시 객체 상태 확인
     * - 로그 출력 시 객체 정보 표시
     */
    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * 객체 동등성 비교
     *
     * 비교 기준: id 값
     * 반환값: boolean
     *
     * 동작 방식:
     * - id가 null이 아니고 같으면 동등한 객체로 판단
     * - id가 null이면 다른 객체로 판단
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Company company = (Company) obj;
        return id != null && id.equals(company.id);
    }

    /**
     * 객체 해시코드 반환
     *
     * 반환값: int
     * 계산 기준: id 값
     *
     * 사용 목적:
     * - HashMap, HashSet 등 해시 기반 컬렉션에서 사용
     * - equals() 메서드와 일관성 유지
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}