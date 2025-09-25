package com.qcheck.qcheck.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL 데이터베이스 연결 상태를 확인하고 관리하는 서비스 클래스
 *
 * 주요 기능:
 * 1. 데이터베이스 연결 상태 테스트
 * 2. Spring Boot 애플리케이션 시작 시 자동으로 DB 연결 확인
 * 3. 연결 실패 시 상세한 문제 해결 가이드 제공
 * 4. 실시간 로그를 통한 연결 상태 모니터링
 *
 * 사용 시나리오:
 * - 개발 환경에서 DB 설정이 올바른지 확인
 * - 운영 환경 배포 시 DB 연결 상태 점검
 * - 시스템 헬스체크의 일부로 활용
 *
 * @author Qcheck Team
 * @since 1.0.0
 */
@Slf4j    // Lombok 어노테이션: 자동으로 log 변수를 생성하여 로깅 기능 제공
@Service  // Spring 어노테이션: 이 클래스를 비즈니스 로직을 담당하는 서비스 빈으로 등록
public class DatabaseConnectionService {

    /**
     * 데이터베이스 연결을 위한 DataSource 객체
     *
     * DataSource란?
     * - 데이터베이스 연결 풀(Connection Pool)을 관리하는 객체
     * - application.properties의 DB 설정 정보를 기반으로 Spring에서 자동 생성
     * - 실제 DB 연결, 해제, 재사용을 효율적으로 관리
     *
     * @Autowired: Spring의 의존성 주입 어노테이션
     * - Spring 컨테이너가 자동으로 적절한 DataSource 빈을 찾아서 주입
     * - 개발자가 직접 객체를 생성할 필요 없음
     */
    @Autowired
    private DataSource dataSource;

    /**
     * application.properties에서 데이터베이스 이름을 주입받는 필드
     *
     * @Value 어노테이션:
     * - Spring의 프로퍼티 값 주입 어노테이션
     * - "${app.db.name}" 표현식으로 application.properties의 app.db.name 값을 읽어옴
     * - 프로퍼티 파일에서 실제 값을 동적으로 가져와 필드에 자동 주입
     *
     * 장점:
     * - 하드코딩 방지: 설정 파일 변경만으로 DB 이름 수정 가능
     * - 환경별 설정: 개발/운영 환경에서 서로 다른 DB 이름 사용 가능
     * - 유지보수성 향상: 설정과 코드의 분리
     *
     * 프로퍼티 파일 위치: src/main/resources/application.properties
     * 해당 설정: app.db.name=qcheck
     */
    @Value("${app.db.name}")
    private String databaseName;

    /**
     * PostgreSQL 데이터베이스 연결 상태를 테스트하는 메서드
     *
     * 수행 작업:
     * 1. DataSource에서 Connection 객체 획득
     * 2. 연결 상태 확인 (null 체크, 연결 열림 상태 확인)
     * 3. 간단한 SQL 쿼리 실행으로 실제 통신 테스트
     * 4. DB 버전, 현재 시간 등 기본 정보 수집
     * 5. 모든 리소스 자동 해제 (try-with-resources 구문 사용)
     *
     * @return boolean
     *         - true: DB 연결 및 쿼리 실행 성공
     *         - false: 연결 실패 또는 쿼리 실행 실패
     *
     * @throws 없음 (모든 예외는 내부에서 처리하고 로그로 기록)
     */
    public boolean testConnection() {
        // try-with-resources 구문: 자동으로 리소스(Connection) 해제 보장
        // connection 변수는 블록이 끝나면 자동으로 close() 호출됨
        try (Connection connection = dataSource.getConnection()) {
            log.info("=== PostgreSQL DB 연결 테스트 시작 ===");

            // 1단계: 기본 연결 상태 확인
            // - connection이 null이 아닌지 확인
            // - 연결이 닫혀있지 않은지 확인 (isClosed() == false)
            if (connection != null && !connection.isClosed()) {
                log.info("✅ DB 연결 성공!");

                // 2단계: 실제 쿼리 실행으로 통신 테스트
                // PostgreSQL에서 동작하는 간단한 테스트 쿼리
                // - SELECT 1: 단순한 상수값 반환 (DB 응답 테스트)
                // - CURRENT_TIMESTAMP: 현재 시간 (DB 시간 설정 확인)
                // - version(): PostgreSQL 버전 정보 (DB 종류와 버전 확인)
                String testQuery = "SELECT 1 as test_value, CURRENT_TIMESTAMP as current_time, version() as db_version";

                // PreparedStatement와 ResultSet도 자동 리소스 해제
                try (PreparedStatement stmt = connection.prepareStatement(testQuery);
                     ResultSet rs = stmt.executeQuery()) {

                    // 쿼리 결과 확인 및 데이터 추출
                    if (rs.next()) {
                        // 각 컬럼별로 데이터 추출
                        int testValue = rs.getInt("test_value");         // 1이 나와야 정상
                        String currentTime = rs.getString("current_time"); // DB 서버의 현재 시간
                        String dbVersion = rs.getString("db_version");     // PostgreSQL 버전 정보

                        // 성공 로그와 함께 추출한 정보 출력
                        log.info("✅ 테스트 쿼리 실행 성공!");
                        log.info("   - 테스트 값: {}", testValue);
                        log.info("   - 현재 시간: {}", currentTime);
                        log.info("   - DB 버전: {}", dbVersion);

                        return true; // 모든 테스트 통과
                    }
                }
            }
        } catch (SQLException e) {
            // SQL 예외 발생 시 상세한 에러 정보 로깅
            // SQLException의 주요 정보:
            // - getMessage(): 사람이 읽을 수 있는 에러 메시지
            // - getErrorCode(): DB 벤더 specific 에러 코드 (PostgreSQL 고유)
            // - getSQLState(): SQL 표준 에러 상태 코드 (SQLSTATE)
            log.error("❌ DB 연결 실패: {}", e.getMessage());
            log.error("   - 에러 코드: {}", e.getErrorCode());
            log.error("   - SQL State: {}", e.getSQLState());
            return false; // 연결 실패
        }

        // 여기까지 도달하면 연결은 되었지만 쿼리 실행에 실패한 경우
        log.error("❌ DB 연결 테스트 실패");
        return false;
    }

    /**
     * Spring Boot 애플리케이션이 완전히 시작된 후 자동으로 실행되는 메서드
     *
     * @EventListener(ApplicationReadyEvent.class) 어노테이션:
     * - Spring Boot의 이벤트 리스너 어노테이션
     * - ApplicationReadyEvent: 애플리케이션이 완전히 준비되었을 때 발생하는 이벤트
     * - 모든 빈 초기화, 설정 로드, 서버 시작이 완료된 후에 실행됨
     *
     * 실행 시점:
     * 1. Spring 컨텍스트 로드 완료
     * 2. 모든 @Component, @Service, @Repository 빈 생성 완료
     * 3. application.properties 설정 적용 완료
     * 4. 내장 웹서버(Tomcat) 시작 완료
     * 5. 이 메서드 실행 ← 여기서 DB 연결 테스트!
     *
     * 장점:
     * - 애플리케이션 시작과 동시에 DB 연결 상태 확인
     * - 문제 발생 시 즉시 로그로 알림
     * - 수동으로 테스트할 필요 없음
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 애플리케이션 시작 완료 알림
        log.info("🚀 Spring Boot 애플리케이션 시작 완료 - DB 연결 테스트 실행");

        // 실제 DB 연결 테스트 메서드 호출
        boolean connectionResult = testConnection();

        // 테스트 결과에 따른 로그 분기 처리
        if (connectionResult) {
            // 성공 시: 긍정적인 메시지로 시스템 정상 동작 확인
            log.info("🎉 DB 연결 테스트 완료 - 모든 시스템이 정상적으로 작동 중입니다!");
        } else {
            // 실패 시: 개발자가 문제를 해결할 수 있는 체크리스트 제공
            log.warn("⚠️  DB 연결 실패 - 데이터베이스 설정을 확인해주세요.");
            log.warn("   📝 확인사항:");
            log.warn("      1. PostgreSQL 서버가 실행 중인지 확인");           // 가장 기본적인 문제
            log.warn("      2. application.properties의 DB 설정 확인");      // 설정 파일 검토
            log.warn("      3. 데이터베이스 '{}' 존재 여부 확인", getDbNameFromProperties()); // DB 스키마 존재 확인
            log.warn("      4. 사용자 권한 확인");                          // 접근 권한 문제

            // 추가로 확인할 수 있는 사항들:
            // - 방화벽 설정
            // - 네트워크 연결
            // - PostgreSQL 포트 (기본 5432) 사용 가능 여부
            // - 사용자명/비밀번호 정확성
        }

        // 테스트 완료 구분선
        log.info("=== PostgreSQL DB 연결 테스트 완료 ===");
    }

    /**
     * application.properties에서 데이터베이스 이름을 추출하는 헬퍼 메서드
     *
     * 기능:
     * - @Value 어노테이션으로 주입된 databaseName 필드 값을 반환
     * - application.properties의 app.db.name 속성값을 실제로 추출
     * - 하드코딩된 값이 아닌 설정 파일의 실제 값 사용
     *
     * 장점:
     * 1. 동적 설정: 프로퍼티 파일 변경만으로 DB 이름 수정 가능
     * 2. 환경별 대응: 개발/테스트/운영 환경별로 다른 DB 이름 설정 가능
     * 3. 일관성 보장: DataSource와 동일한 설정값 사용으로 일관성 유지
     * 4. 디버깅 용이: 로그에서 실제 사용 중인 DB 이름 확인 가능
     *
     * 사용 예시:
     * - 에러 로그: "데이터베이스 'qcheck_dev' 연결 실패"
     * - 성공 로그: "데이터베이스 'qcheck_prod' 연결 성공"
     *
     * @return String application.properties의 app.db.name 값
     *               (예: "qcheck", "qcheck_dev", "qcheck_test" 등)
     */
    private String getDbNameFromProperties() {
        // @Value로 주입된 실제 DB 이름 반환
        return databaseName;
    }
}