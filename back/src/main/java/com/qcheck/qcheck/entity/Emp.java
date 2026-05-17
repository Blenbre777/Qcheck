package com.qcheck.qcheck.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 사원(사용자) 엔티티 클래스
 *
 * 목적: 사원(사용자) 정보를 데이터베이스의 emp 테이블과 매핑
 * 테이블명: emp
 * 주요 기능: 사원의 로그인, 회사 소속, 권한 정보 관리
 *
 * 필드 구성:
 * - empSeq: 사원 고유 식별자 (기본키, 자동 증가)
 * - empId: 로그인 아이디 (중복 불가)
 * - empPw: 로그인 비밀번호 (암호화 저장)
 * - empNm: 사원 이름
 * - companySeq: 소속 회사 식별자 (외래키)
 * - empEmail: 사원 이메일 (중복 불가)
 * - empRole: 권한 (SUPER_ADMIN, COMPANY_ADMIN, USER)
 * - useYn: 사용 여부 (Y/N)
 * - createdAt: 생성 시간
 * - createdEp: 생성자
 * - updatedAt: 수정 시간
 * - updatedEp: 수정자
 */
@Entity
@Table(name = "emp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Emp {

    /**
     * 사원 고유 식별자
     *
     * 자료형: Long
     * 데이터베이스: BIGSERIAL (PostgreSQL 자동 증가)
     * 제약조건: PRIMARY KEY, NOT NULL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_seq")
    private Long empSeq;

    /**
     * 로그인 아이디
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(50)
     * 제약조건: NOT NULL, UNIQUE
     */
    @Column(name = "emp_id", nullable = false, length = 50, unique = true)
    private String empId;

    /**
     * 로그인 비밀번호 (암호화됨)
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(255)
     * 제약조건: NOT NULL
     * 주의: BCrypt 등으로 암호화하여 저장
     */
    @Column(name = "emp_pw", nullable = false, length = 255)
    private String empPw;

    /**
     * 사원 이름
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(100)
     * 제약조건: NOT NULL
     */
    @Column(name = "emp_nm", nullable = false, length = 100)
    private String empNm;

    /**
     * 소속 회사 식별자 (외래키)
     *
     * 자료형: Long
     * 데이터베이스: BIGINT
     * 제약조건: NOT NULL, REFERENCES company(company_seq)
     * 목적: 멀티테넌트 격리를 위한 필수 필드
     */
    @Column(name = "company_seq", nullable = false)
    private Long companySeq;

    /**
     * 사원 이메일
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(255)
     * 제약조건: NOT NULL, UNIQUE
     * 사용처: JWT 토큰, 로그인 시 인증 정보
     */
    @Column(name = "emp_email", nullable = false, length = 255, unique = true)
    private String empEmail;

    /**
     * 사원 권한
     *
     * 자료형: EmpRole (enum)
     * 데이터베이스: VARCHAR(20)
     * 제약조건: NOT NULL, DEFAULT 'USER'
     * 가능한 값:
     * - SUPER_ADMIN: 시스템 관리자
     * - COMPANY_ADMIN: 회사 관리자
     * - USER: 일반 사용자
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "emp_role", nullable = false, length = 20)
    @Builder.Default
    private EmpRole empRole = EmpRole.USER;

    /**
     * 사용 여부
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(2)
     * 제약조건: NOT NULL, DEFAULT 'Y'
     * 가능한 값: Y (사용), N (미사용)
     */
    @Column(name = "use_yn", nullable = false, length = 2)
    @Builder.Default
    private String useYn = "Y";

    /**
     * 생성 시간
     *
     * 자료형: LocalDateTime
     * 데이터베이스: TIMESTAMP WITH TIME ZONE
     * 제약조건: NOT NULL, DEFAULT CURRENT_TIMESTAMP
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 생성자
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(50)
     * 제약조건: NOT NULL, DEFAULT 'SYSTEM'
     */
    @Column(name = "created_ep", nullable = false, length = 50)
    @Builder.Default
    private String createdEp = "SYSTEM";

    /**
     * 수정 시간
     *
     * 자료형: LocalDateTime
     * 데이터베이스: TIMESTAMP WITH TIME ZONE
     * 제약조건: DEFAULT NULL
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 수정자
     *
     * 자료형: String
     * 데이터베이스: VARCHAR(50)
     * 제약조건: DEFAULT NULL
     */
    @Column(name = "updated_ep", length = 50)
    private String updatedEp;

    @Override
    public String toString() {
        return "Emp{" +
                "empSeq=" + empSeq +
                ", empId='" + empId + '\'' +
                ", empNm='" + empNm + '\'' +
                ", companySeq=" + companySeq +
                ", empEmail='" + empEmail + '\'' +
                ", empRole=" + empRole +
                ", useYn='" + useYn + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Emp emp = (Emp) obj;
        return empSeq != null && empSeq.equals(emp.empSeq);
    }

    @Override
    public int hashCode() {
        return empSeq != null ? empSeq.hashCode() : 0;
    }
}
