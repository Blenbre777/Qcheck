# =============================================================================
# 프롬프트 템플릿 생성 스크립트 (PowerShell)
# =============================================================================
# 다양한 코드리뷰 상황에 맞는 특화 프롬프트 템플릿들을 생성합니다.
# =============================================================================

# UTF-8 인코딩 설정 (강화)
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['Out-File:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Add-Content:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Set-Content:Encoding'] = 'UTF8'

# UTF-8 인코딩 객체 생성 (BOM 없음)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# 색상 출력 함수
function Write-ColorText {
    param(
        [string]$Text,
        [string]$Color = "White"
    )
    Write-Host $Text -ForegroundColor $Color
}

# 템플릿 디렉토리 생성
$TemplateDir = "review-output\templates"
if (!(Test-Path $TemplateDir)) {
    New-Item -ItemType Directory -Path $TemplateDir -Force | Out-Null
}

Write-ColorText "[INFO] 코드리뷰 전용 프롬프트 템플릿 생성 (PowerShell)" "Cyan"

# 1. 보안 중심 리뷰 템플릿
$securityTemplate = @"
# 보안 중심 코드리뷰 요청

## [목표] 보안 검토 관점

다음 코드에 대해 **보안 취약점** 중심으로 철저한 검토를 수행해주세요.

### 주요 검토 항목

#### 1. **웹 애플리케이션 보안**
- **SQL 인젝션**: PreparedStatement 사용, 동적 쿼리 검증
- **XSS (Cross-Site Scripting)**: 입력값 sanitization, 출력 인코딩
- **CSRF (Cross-Site Request Forgery)**: 토큰 검증, SameSite 쿠키
- **세션 관리**: 세션 하이재킹, 세션 고정 공격 방지

#### 2. **인증 및 권한 처리**
- **패스워드 보안**: 해싱 알고리즘, Salt 사용
- **토큰 관리**: JWT 보안, 토큰 만료 처리
- **권한 검증**: 수직/수평 권한 상승 방지
- **API 보안**: 인증 헤더, Rate Limiting

#### 3. **데이터 보호**
- **민감정보 노출**: 로그, 에러 메시지에서 정보 유출
- **데이터 암호화**: 저장 시/전송 시 암호화
- **개인정보 처리**: GDPR, 개인정보보호법 준수
- **데이터 검증**: 입력값 타입/길이/형식 검증

#### 4. **서버 보안**
- **파일 업로드**: 확장자 검증, 경로 순회 공격 방지
- **디렉토리 순회**: Path traversal 공격 방지
- **리소스 제한**: DoS 공격 방지, 메모리/CPU 사용량 제한
- **에러 처리**: 스택 트레이스 노출 방지

### [체크리스트] 보안 체크리스트

각 항목에 대해 [OK] 안전 / [WARN] 주의 / [FAIL] 위험으로 평가해주세요:

- [ ] SQL 쿼리 보안
- [ ] 사용자 입력 검증
- [ ] 권한 확인 로직
- [ ] 세션/토큰 관리
- [ ] 에러 정보 노출
- [ ] 파일 처리 보안
- [ ] 로깅 보안
- [ ] 외부 API 호출 보안

### [개선] 보안 개선 제안

발견된 각 취약점에 대해:
1. **위험도 평가** (Critical/High/Medium/Low)
2. **공격 시나리오** 설명
3. **구체적인 수정 방법**
4. **보안 테스트 방안**

## [코드] 검토 대상 코드
{여기에 코드가 삽입됩니다}
"@

[System.IO.File]::WriteAllText("$TemplateDir\security-review-prompt.txt", $securityTemplate, $utf8NoBom)

# 2. 성능 최적화 템플릿
$performanceTemplate = @"
# ⚡ 성능 최적화 코드리뷰 요청

## [목표] 성능 최적화 관점

다음 코드에 대해 **성능 최적화** 관점에서 상세한 분석을 수행해주세요.

### [영역] 주요 검토 영역

#### 1. **데이터베이스 성능**
- **N+1 쿼리 문제**: 지연 로딩, Fetch Join 활용
- **쿼리 최적화**: 인덱스 활용, 실행계획 분석
- **커넥션 풀**: 적절한 커넥션 수, 타임아웃 설정
- **캐싱 전략**: 1차/2차 캐시, Redis 활용

#### 2. **알고리즘 및 자료구조**
- **시간 복잡도**: Big-O 분석, 비효율적 알고리즘 개선
- **공간 복잡도**: 메모리 사용량 최적화
- **자료구조 선택**: 적절한 Collection 사용
- **루프 최적화**: 불필요한 반복 제거

#### 3. **메모리 관리**
- **메모리 누수**: 리소스 해제, WeakReference 활용
- **가비지 컬렉션**: GC 부담 최소화
- **객체 생성**: 불필요한 객체 생성 방지
- **스트림 처리**: 대용량 데이터 처리 최적화

#### 4. **네트워크 및 I/O**
- **HTTP 요청**: 배치 처리, 압축 활용
- **파일 I/O**: 버퍼링, NIO 활용
- **직렬화**: 효율적인 직렬화 방식
- **캐싱**: CDN, 브라우저 캐시 활용

#### 5. **Frontend 성능**
- **번들 크기**: Tree shaking, Code splitting
- **렌더링**: Virtual DOM, Change Detection 최적화
- **네트워크**: Lazy loading, Prefetching
- **메모리**: 이벤트 리스너 해제, 메모리 누수 방지

### [측정] 성능 측정 지표

각 영역별 측정 가능한 지표:

#### Backend
- 응답 시간 (Response Time)
- 처리량 (Throughput)
- 메모리 사용량
- CPU 사용률
- 데이터베이스 쿼리 시간

#### Frontend
- 페이지 로드 시간
- First Contentful Paint (FCP)
- Time to Interactive (TTI)
- 번들 크기
- 메모리 사용량

### 최적화 우선순위

성능 개선 효과에 따른 우선순위:

1. **[HIGH IMPACT]**: 즉시 개선 필요
2. **[MEDIUM IMPACT]**: 계획적 개선
3. **[LOW IMPACT]**: 여유 시 개선

### [제안] 개선 제안 형식

각 성능 이슈에 대해:
1. **현재 문제점** 분석
2. **성능 영향도** 측정
3. **개선 방법** 제시 (코드 예제 포함)
4. **예상 효과** 정량적 설명
5. **구현 난이도** 평가

## [코드] 분석 대상 코드
{여기에 코드가 삽입됩니다}
"@

[System.IO.File]::WriteAllText("$TemplateDir\performance-review-prompt.txt", $performanceTemplate, $utf8NoBom)

# 3. 아키텍처 리뷰 템플릿
$architectureTemplate = @"
# [아키텍처] 아키텍처 리뷰 요청

## [목표] 아키텍처 검토 관점

다음 코드에 대해 **소프트웨어 아키텍처** 관점에서 종합적인 분석을 수행해주세요.

### [도구] Spring Boot 아키텍처

#### 1. **계층 분리 (Layered Architecture)**
- **Controller**: REST API 설계, HTTP 매핑
- **Service**: 비즈니스 로직 분리, 트랜잭션 관리
- **Repository**: 데이터 접근 추상화
- **Entity**: 도메인 모델 설계

#### 2. **의존성 주입 (Dependency Injection)**
- **@Autowired vs Constructor Injection**
- **인터페이스 기반 설계**
- **순환 의존성 방지**
- **테스트 가능한 설계**

#### 3. **설정 및 프로파일**
- **@Configuration 클래스 구조**
- **프로파일별 설정 분리**
- **외부 설정 관리**
- **빈 라이프사이클 관리**

### [프레임워크] Angular 아키텍처

#### 1. **컴포넌트 설계**
- **단일 책임 원칙** 준수
- **컴포넌트 간 통신** 패턴
- **상태 관리** 전략
- **라이프사이클 훅** 활용

#### 2. **서비스 및 의존성**
- **싱글톤 서비스** 설계
- **HTTP 클라이언트** 활용
- **에러 핸들링** 전략
- **인터셉터** 활용

#### 3. **모듈 구조**
- **Feature Module** 분리
- **Shared Module** 설계
- **Core Module** 구성
- **Lazy Loading** 적용

### [원칙] 설계 원칙 검토

#### SOLID 원칙
- **Single Responsibility**: 단일 책임
- **Open/Closed**: 확장/수정 원칙
- **Liskov Substitution**: 리스코프 치환
- **Interface Segregation**: 인터페이스 분리
- **Dependency Inversion**: 의존성 역전

#### 기타 설계 원칙
- **DRY (Don't Repeat Yourself)**
- **KISS (Keep It Simple, Stupid)**
- **YAGNI (You Aren't Gonna Need It)**
- **관심사 분리 (Separation of Concerns)**

### 패턴 적용

#### 디자인 패턴
- **Factory Pattern**: 객체 생성 추상화
- **Strategy Pattern**: 알고리즘 교체 가능
- **Observer Pattern**: 이벤트 기반 설계
- **Decorator Pattern**: 기능 확장

#### 아키텍처 패턴
- **MVC (Model-View-Controller)**
- **Repository Pattern**: 데이터 접근 추상화
- **DTO Pattern**: 데이터 전송 객체
- **Builder Pattern**: 복잡한 객체 생성

### 품질 속성

#### 1. **확장성 (Scalability)**
- 수평/수직 확장 가능성
- 마이크로서비스 전환 가능성
- 부하 분산 고려사항

#### 2. **유지보수성 (Maintainability)**
- 코드 가독성 및 이해도
- 변경 영향도 최소화
- 테스트 용이성

#### 3. **성능 (Performance)**
- 응답 시간 최적화
- 리소스 사용 효율성
- 병목 지점 식별

#### 4. **보안 (Security)**
- 아키텍처 수준 보안
- 인증/권한 설계
- 데이터 보호 전략

### [제안] 개선 제안

각 아키텍처 이슈에 대해:
1. **현재 구조** 분석
2. **문제점** 식별
3. **개선 방향** 제시
4. **리팩토링 계획** 수립
5. **마이그레이션 전략** 제안

## [코드] 분석 대상 코드
{여기에 코드가 삽입됩니다}
"@

[System.IO.File]::WriteAllText("$TemplateDir\architecture-review-prompt.txt", $architectureTemplate, $utf8NoBom)

# 4. 신규 기능 리뷰 템플릿
$newFeatureTemplate = @"
# 🆕 신규 기능 코드리뷰 요청

## [목표] 신규 기능 검토 관점

새로 추가된 기능에 대해 **포괄적인 품질 검토**를 수행해주세요.

### [요구사항] 기능 요구사항 검토

#### 1. **기능 완성도**
- 요구사항 충족 여부
- 예외 상황 처리
- 사용자 시나리오 커버리지
- 에지 케이스 처리

#### 2. **사용성 (Usability)**
- 직관적인 API 설계
- 사용자 친화적 UI/UX
- 에러 메시지 명확성
- 도움말 및 가이드

### [품질] 기술적 품질 검토

#### 1. **코드 품질**
- 가독성 및 명확성
- 네이밍 컨벤션
- 주석 및 문서화
- 코드 복잡도

#### 2. **설계 품질**
- 모듈화 및 재사용성
- 확장 가능성
- 기존 시스템과의 통합
- 의존성 관리

#### 3. **성능 고려사항**
- 응답 시간 최적화
- 메모리 효율성
- 데이터베이스 쿼리 최적화
- 캐싱 전략

### [테스트] 테스트 전략

#### 1. **단위 테스트**
- 핵심 로직 테스트 커버리지
- Mock 객체 활용
- 경계값 테스트
- 예외 상황 테스트

#### 2. **통합 테스트**
- API 엔드포인트 테스트
- 데이터베이스 연동 테스트
- 외부 서비스 연동 테스트
- 사용자 시나리오 테스트

#### 3. **성능 테스트**
- 부하 테스트 필요성
- 스트레스 테스트 계획
- 메모리 누수 검증
- 동시성 테스트

### [문서] 문서화 요구사항

#### 1. **API 문서**
- Swagger/OpenAPI 문서화
- 요청/응답 예제
- 에러 코드 정의
- 변경 이력 관리

#### 2. **사용자 문서**
- 기능 설명서
- 사용 가이드
- FAQ 준비
- 릴리즈 노트

### [배포] 배포 준비사항

#### 1. **배포 계획**
- 단계별 배포 전략
- 롤백 계획
- 모니터링 설정
- 알림 구성

#### 2. **운영 고려사항**
- 로그 및 메트릭
- 에러 모니터링
- 성능 모니터링
- 용량 계획

### [체크] 체크리스트

신규 기능에 대한 종합 점검:

#### 기능성
- [ ] 요구사항 완전 구현
- [ ] 예외 상황 적절 처리
- [ ] 사용자 시나리오 검증
- [ ] 데이터 검증 로직

#### 품질
- [ ] 코드 리뷰 완료
- [ ] 테스트 작성 완료
- [ ] 문서화 완료
- [ ] 보안 검토 완료

#### 운영
- [ ] 모니터링 설정
- [ ] 로깅 구현
- [ ] 에러 핸들링
- [ ] 성능 최적화

### [평가] 종합 평가

다음 기준으로 종합 평가를 제공해주세요:

1. **Ready for Production** [OK]
   - 즉시 배포 가능한 상태

2. **Minor Issues** [WARN]
   - 작은 개선사항 필요

3. **Major Issues** [FAIL]
   - 중요한 수정사항 필요

4. **Needs Rework**
   - 재작업 필요

## [코드] 검토 대상 코드
{여기에 코드가 삽입됩니다}
"@

[System.IO.File]::WriteAllText("$TemplateDir\new-feature-review-prompt.txt", $newFeatureTemplate, $utf8NoBom)

# 5. 사용 가이드
$usageGuide = @"
# [가이드] 코드리뷰 프롬프트 템플릿 사용 가이드 (Windows)

## [종류] 템플릿 종류

### 1. **기본 템플릿** (`review-prompt.txt`)
- 전체적인 코드 품질 검토
- 일반적인 코드리뷰 상황에 적합

### 2. **보안 중심 템플릿** (`security-review-prompt.txt`)
- 보안 취약점 집중 검토
- 민감한 기능 개발 시 사용

### 3. **성능 최적화 템플릿** (`performance-review-prompt.txt`)
- 성능 병목 지점 분석
- 대용량 처리, 응답 시간 개선 시 사용

### 4. **아키텍처 템플릿** (`architecture-review-prompt.txt`)
- 설계 구조 전반적 검토
- 리팩토링, 시스템 개선 시 사용

### 5. **신규 기능 템플릿** (`new-feature-review-prompt.txt`)
- 새로운 기능 개발 시 종합 검토
- 배포 전 최종 점검 시 사용

## [사용법] Windows에서 사용 방법

### PowerShell 사용
```powershell
# 코드 추출
.\scripts\extract-code.ps1 -Changed

# 적절한 템플릿 선택
Copy-Item "review-output\templates\security-review-prompt.txt" "review-output\custom-prompt.txt"
```

### 명령 프롬프트 (CMD) 사용
```cmd
REM 코드 추출
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed

REM 템플릿 복사
copy "review-output\templates\security-review-prompt.txt" "review-output\custom-prompt.txt"
```

### 배치 파일 사용
```cmd
REM 간편한 실행을 위한 배치 파일 활용
code-review-changed.bat
code-review-all.bat
code-review-security.bat
```

## [팁] Windows 환경 팁

### PowerShell 실행 정책 설정
```powershell
# 현재 사용자에 대해 스크립트 실행 허용
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 파일 인코딩 주의사항
- 모든 출력 파일은 UTF-8 인코딩으로 생성됩니다
- Windows 메모장 대신 VS Code나 기타 편집기 사용 권장

### 경로 구분자
- Windows에서는 백슬래시(`\`) 사용
- PowerShell에서는 슬래시(`/`)와 백슬래시(`\`) 모두 지원

## [해결] 문제 해결

### PowerShell 스크립트 실행 오류
```powershell
# 실행 정책 확인
Get-ExecutionPolicy

# 일회성 실행 (권장)
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
```

### Git 명령어 오류
- Git for Windows 설치 확인
- 환경변수 PATH에 Git 경로 추가

### 파일 권한 오류
- 관리자 권한으로 PowerShell 실행
- 또는 사용자 홈 디렉토리에서 실행

## [특화] Windows 특화 기능

### Windows 탐색기 통합
- 프로젝트 폴더에서 우클릭 > "PowerShell에서 열기"
- 또는 "명령 프롬프트에서 열기"

### 결과 파일 자동 열기
```powershell
# 생성된 프롬프트 파일 자동으로 메모장에서 열기
notepad "review-output\review-prompt.txt"

# VS Code에서 열기 (VS Code 설치된 경우)
code "review-output\review-prompt.txt"
```

### 배치 파일로 자동화
```cmd
@echo off
echo 코드리뷰 시스템 시작...
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
pause
```

## [커스터마이징] 커스터마이징

### 윈도우 환경변수 활용
```cmd
set QCHECK_OUTPUT_DIR=C:\CodeReview\Output
set QCHECK_MAX_FILE_SIZE=1000
```

### 사용자별 설정 파일
- `%USERPROFILE%\.qcheck-config` 파일 생성
- 개인 설정으로 기본 설정 오버라이드

## [워크플로우] 권장 워크플로우 (Windows)

1. **PowerShell 터미널 열기**
2. **프로젝트 디렉토리로 이동**
3. **코드 추출 실행**
4. **결과 파일 VS Code나 메모장에서 열기**
5. **Claude에게 프롬프트 복사-붙여넣기**
6. **리뷰 결과를 파일로 저장**

---

**[TIP]**: 자주 사용하는 명령어는 배치 파일(.bat)로 만들어 바탕화면에 두면 편리합니다!
"@

[System.IO.File]::WriteAllText("$TemplateDir\usage-guide-windows.md", $usageGuide, $utf8NoBom)

Write-ColorText "[SUCCESS] PowerShell 프롬프트 템플릿 생성 완료!" "Green"
Write-ColorText "[INFO] 생성된 템플릿:" "Cyan"
Write-Host "  - security-review-prompt.txt (보안 중심)"
Write-Host "  - performance-review-prompt.txt (성능 최적화)"
Write-Host "  - architecture-review-prompt.txt (아키텍처)"
Write-Host "  - new-feature-review-prompt.txt (신규 기능)"
Write-Host "  - usage-guide-windows.md (Windows 사용 가이드)"