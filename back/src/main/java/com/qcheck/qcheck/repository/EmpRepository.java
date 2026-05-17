package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Emp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사원(사용자) 저장소 (JPA Repository)
 *
 * 책임:
 * - 사원(emp) 테이블 CRUD 작업
 * - 사원 정보 조회 (by empId, empEmail, etc.)
 *
 * 주요 쿼리 메서드:
 * - findByEmpId: 로그인 아이디로 사원 조회
 * - findByEmpEmail: 이메일로 사원 조회 (JWT 인증 시 사용)
 * - findByCompanySeq: 특정 회사의 모든 사원 조회
 */
@Repository
public interface EmpRepository extends JpaRepository<Emp, Long> {

    /**
     * 로그인 아이디로 사원 조회
     *
     * @param empId 로그인 아이디
     * @return Optional<Emp>
     */
    Optional<Emp> findByEmpId(String empId);

    /**
     * 이메일로 사원 조회
     *
     * 용도: JWT 인증, 회원가입 시 이메일 중복 확인
     *
     * @param empEmail 사원 이메일
     * @return Optional<Emp>
     */
    Optional<Emp> findByEmpEmail(String empEmail);

    /**
     * 회사 ID로 사원 수 조회
     *
     * @param companySeq 회사 ID
     * @return 사원 수
     */
    long countByCompanySeq(Long companySeq);

    /**
     * 회사 ID와 이메일로 사원 조회 (멀티테넌트 격리)
     *
     * @param companySeq 회사 ID
     * @param empEmail 사원 이메일
     * @return Optional<Emp>
     */
    Optional<Emp> findByCompanySeqAndEmpEmail(Long companySeq, String empEmail);

    /**
     * 사원 ID와 회사 ID로 사원 조회 (RefreshToken 갱신 시 사용)
     *
     * @param empSeq 사원 ID
     * @param companySeq 회사 ID
     * @return Optional<Emp>
     */
    Optional<Emp> findByEmpSeqAndCompanySeq(Long empSeq, Long companySeq);
}
