package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 부품 저장소 (JPA Repository)
 *
 * 책임:
 * - 부품(part) 테이블 CRUD 작업
 * - 회사별, 활성화 상태별 부품 조회
 *
 * 쿼리 메서드:
 * - findByCompanySeq: 회사별 모든 부품 조회
 * - findByCompanySeqAndUseYn: 회사별 활성화 부품만 조회 (드롭다운)
 * - findByCompanySeqAndPartNm: 회사별 부품명 중복 확인
 * - findByCompanySeqAndPartSeq: 부품 상세 조회 (권한 확인용)
 */
@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    /**
     * 회사별 모든 부품 조회
     *
     * @param companySeq 회사 ID
     * @return 부품 목록 (삭제된 항목 포함)
     */
    List<Part> findByCompanySeq(Long companySeq);

    /**
     * 회사별 활성화된 부품만 조회
     *
     * 용도: 불량 등록 폼의 부품 드롭다운
     *
     * @param companySeq 회사 ID
     * @param useYn 사용 여부 (Y)
     * @return 활성화 부품 목록
     */
    List<Part> findByCompanySeqAndUseYn(Long companySeq, String useYn);

    /**
     * 회사별 부품명 중복 확인
     *
     * @param companySeq 회사 ID
     * @param partNm 부품명
     * @return Optional<Part>
     */
    Optional<Part> findByCompanySeqAndPartNm(Long companySeq, String partNm);

    /**
     * 부품 상세 조회 (권한 확인용)
     *
     * 부품 수정/삭제 전 접근 권한 확인 시 사용
     *
     * @param partSeq 부품 ID
     * @param companySeq 회사 ID
     * @return Optional<Part>
     */
    Optional<Part> findByPartSeqAndCompanySeq(Long partSeq, Long companySeq);
}
