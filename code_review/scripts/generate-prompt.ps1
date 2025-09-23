# =============================================================================
# 프롬프트 생성 스크립트 (PowerShell) - Claude 코드리뷰용
# =============================================================================
# 코드 추출된 내용을 바탕으로 Claude에게 보낼 프롬프트를 생성합니다.
# =============================================================================


param(
    [string]$Mode = "basic"
)

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

# 설정 로드
$ConfigFile = ".code-review-config"
$OutputDir = "review-output"

if (Test-Path $ConfigFile) {
    Get-Content $ConfigFile | ForEach-Object {
        if ($_ -match '^OUTPUT_DIR=(.*)$') {
            $OutputDir = $matches[1].Trim().Trim('"')
        }
    }
}

# 함수: 기본 프롬프트 템플릿 생성
function New-BasicPrompt {
    $promptFile = Join-Path $OutputDir "review-prompt.txt"
    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    # 파일 존재 확인
    if (!(Test-Path $codeFile) -or !(Test-Path $fileListFile)) {
        Write-ColorText "[ERROR] 필요한 파일이 없습니다: $codeFile 또는 $fileListFile" "Red"
        exit 1
    }

    # 파일 통계
    $fileCount = (Get-Content $fileListFile | Measure-Object -Line).Lines
    $lineCount = (Get-Content $codeFile | Measure-Object -Line).Lines

    # 기본 프롬프트 템플릿
    $promptTemplate = @"
# 프로젝트 정보
- **프로젝트명**: Qcheck (질문 체크 시스템)
- **백엔드**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate
- **프론트엔드**: Angular 20, TypeScript, SCSS
- **아키텍처**: RESTful API, SPA (Single Page Application)

# 코드리뷰 요청

다음 코드에 대해 전문적이고 상세한 코드리뷰를 수행해주세요.

## [목표] 리뷰 관점

### 1. **코드 품질 (Code Quality)**
- 가독성: 변수명, 메서드명의 명확성
- 유지보수성: 코드 구조, 모듈화
- 재사용성: 중복 코드 제거, 공통 기능 추출
- 일관성: 코딩 컨벤션 준수

### 2. **성능 최적화 (Performance)**
- 비효율적인 알고리즘이나 로직
- 메모리 사용 최적화
- 데이터베이스 쿼리 최적화 (N+1 문제, 인덱스 활용)
- 불필요한 연산 제거

### 3. **보안 (Security)**
- SQL 인젝션, XSS 등 웹 취약점
- 입력값 검증 및 sanitization
- 권한 처리 및 인증/인가
- 민감 정보 노출 방지

### 4. **아키텍처 및 설계 (Architecture & Design)**
- Spring Boot 베스트 프랙티스 적용
- Angular 아키텍처 패턴 준수
- 의존성 주입 및 IoC 활용
- 계층 분리 (Controller, Service, Repository)
- 디자인 패턴 적용

### 5. **예외 처리 및 안정성 (Error Handling & Reliability)**
- 예외 처리 전략
- null 체크 및 방어적 프로그래밍
- 경계 조건 처리
- 리소스 관리 (Connection, Stream 등)

### 6. **테스트 및 품질 보증 (Testing & Quality Assurance)**
- 단위 테스트 작성 가능성
- 통합 테스트 고려사항
- 테스트 커버리지 개선 방안
- Mock 객체 활용

### 7. **문서화 및 주석 (Documentation)**
- JavaDoc/TSDoc 작성
- 복잡한 로직에 대한 설명
- API 문서화 필요성

## [통계] 코드 통계
- **총 파일 수**: ${fileCount}개
- **총 라인 수**: ${lineCount}줄

## [파일] 파일 목록
```
"@

    # 파일 목록 추가
    [System.IO.File]::WriteAllText($promptFile, $promptTemplate, $utf8NoBom)
    $fileListContent = Get-Content $fileListFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $fileListContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # 코드 내용 추가
    [System.IO.File]::AppendAllText($promptFile, "## [CODE] 코드 내용$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $codeContent = Get-Content $codeFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $codeContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # 요청사항 추가
    $requestTemplate = @"
## [REQUEST] 요청사항

### 1. **이슈 분석 및 분류**
각 발견된 이슈에 대해 다음과 같이 분류해주세요:

- **[CRITICAL]**: 보안 취약점, 심각한 버그 가능성
- **[HIGH]**: 성능 이슈, 아키텍처 문제
- **[MEDIUM]**: 코드 품질, 유지보수성 개선
- **[LOW]**: 코딩 컨벤션, 스타일 가이드

### 2. **구체적인 개선 방안**
- 문제점 지적뿐만 아니라 **구체적인 해결 방법** 제시
- **Before/After 코드 예제** 포함
- **왜 그렇게 개선해야 하는지** 이유 설명

### 3. **프레임워크별 베스트 프랙티스**
- **Spring Boot**: @Service, @Repository, @Transactional 등의 올바른 사용
- **JPA/Hibernate**: 엔티티 설계, 쿼리 최적화, 지연 로딩
- **Angular**: 컴포넌트 설계, 서비스 패턴, RxJS 활용
- **TypeScript**: 타입 안정성, 인터페이스 활용

### 4. **우선순위 제안**
개선사항을 우선순위에 따라 정렬하여 제시해주세요.

### 5. **추가 고려사항**
- 향후 확장성을 위한 제안
- 성능 모니터링 포인트
- 추가 테스트가 필요한 영역

---

**[참고]**: 단순한 문제 지적보다는 **교육적 가치**가 있는 리뷰를 부탁드립니다. 각 제안사항이 **왜 중요한지**, **어떤 이점을 가져다주는지** 설명해주시면 더욱 도움이 됩니다.
"@

    [System.IO.File]::AppendAllText($promptFile, $requestTemplate, $utf8NoBom)

    Write-ColorText "[SUCCESS] 기본 프롬프트 생성 완료: $promptFile" "Green"
}

# 함수: 변경분 전용 프롬프트 생성
function New-ChangedPrompt {
    $promptFile = Join-Path $OutputDir "review-prompt.txt"
    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"
    $diffFile = Join-Path $OutputDir "diff-summary.txt"

    # Git diff 정보 확인
    if (!(Test-Path $diffFile)) {
        Write-ColorText "[WARNING] Git diff 파일이 없어 기본 프롬프트로 생성합니다." "Yellow"
        New-BasicPrompt
        return
    }

    # 파일 통계
    $fileCount = (Get-Content $fileListFile | Measure-Object -Line).Lines
    $lineCount = (Get-Content $codeFile | Measure-Object -Line).Lines

    # Git diff 통계 추출
    $diffStats = Get-Content $diffFile -TotalCount 20 | Where-Object { $_ -match "files? changed|insertions?|deletions?" } | Select-Object -First 1

    # 변경분 프롬프트 템플릿
    $promptTemplate = @"
# 변경분 코드리뷰 요청

## 프로젝트 정보
- **프로젝트명**: Qcheck (질문 체크 시스템)
- **백엔드**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate
- **프론트엔드**: Angular 20, TypeScript, SCSS
- **아키텍처**: RESTful API, SPA (Single Page Application)

## 변경 요약
- **변경된 파일**: ${fileCount}개
- **총 라인 수**: ${lineCount}줄
"@

    if ($diffStats) {
        $promptTemplate += "`n- **Git 통계**: $diffStats"
    }

    $promptTemplate += "`n`n## [통계] Git Diff 요약`n``````diff"

    # 프롬프트 파일 생성
    [System.IO.File]::WriteAllText($promptFile, $promptTemplate, $utf8NoBom)
    $diffContent = Get-Content $diffFile -TotalCount 50 -Encoding UTF8 | Out-String
    [System.IO.File]::AppendAllText($promptFile, $diffContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # 파일 목록 추가
    [System.IO.File]::AppendAllText($promptFile, "## [파일] 변경된 파일 목록$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $fileListContent = Get-Content $fileListFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $fileListContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # 코드 내용 추가
    [System.IO.File]::AppendAllText($promptFile, "## [CODE] 변경된 파일별 전체 코드$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $codeContent = Get-Content $codeFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $codeContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # 변경분 리뷰 요청사항 추가
    $changedRequestTemplate = @"
## [목표] 변경분 리뷰 요청사항

### 1. **변경 영향도 분석**
- 이번 변경이 **기존 코드에 미치는 영향** 분석
- **연관된 컴포넌트나 모듈**에 대한 영향도 평가
- **데이터베이스 스키마나 API 변경**으로 인한 호환성 이슈

### 2. **변경사항 품질 검토**
- 새로 추가된 코드의 **품질 평가**
- 수정된 로직의 **정확성 및 효율성**
- **코딩 표준 및 컨벤션** 준수 여부

### 3. **리스크 평가**
- **[HIGH RISK]**: 즉시 수정이 필요한 심각한 문제
- **[MEDIUM RISK]**: 주의 깊게 모니터링이 필요한 부분
- **[LOW RISK]**: 개선하면 좋을 부분

### 4. **호환성 및 안정성**
- **기존 API 호환성** 유지 여부
- **데이터베이스 마이그레이션** 필요성
- **의존성 변경**으로 인한 부작용 가능성

### 5. **테스트 전략**
- **새로 추가된 기능**에 대한 테스트 방안
- **회귀 테스트**가 필요한 영역 식별
- **통합 테스트** 시나리오 제안

### 6. **배포 고려사항**
- **배포 전 체크리스트**
- **롤백 계획** 필요성
- **점진적 배포(Blue-Green, Canary)** 필요 여부

### 7. **문서화 요구사항**
- **API 문서** 업데이트 필요성
- **사용자 매뉴얼** 변경 사항
- **개발팀 공유** 필요 정보

## [체크리스트] 우선순위별 액션 아이템

리뷰 결과를 다음과 같이 정리해주세요:

### 즉시 수정 필요 (Critical)
- [ ] 수정해야 할 심각한 문제들

### [HIGH] 배포 전 수정 권장
- [ ] 배포하기 전에 개선하면 좋을 사항들

### [개선] 향후 개선 사항 (Medium/Low)
- [ ] 시간이 날 때 개선할 수 있는 사항들

---

**[목표]**: 안전하고 품질 높은 코드 변경을 통한 시스템 개선
"@

    [System.IO.File]::AppendAllText($promptFile, $changedRequestTemplate, $utf8NoBom)

    Write-ColorText "[SUCCESS] 변경분 전용 프롬프트 생성 완료: $promptFile" "Green"
}

# 메인 실행
Write-ColorText "[INFO] Claude 코드리뷰 프롬프트 생성 (PowerShell)" "Cyan"

switch ($Mode) {
    "changed" {
        New-ChangedPrompt
    }
    default {
        New-BasicPrompt
    }
}

# 추가 템플릿 파일들 생성
if (Test-Path "scripts\create-prompt-templates.ps1") {
    & "scripts\create-prompt-templates.ps1"
}

Write-ColorText "[SUCCESS] 프롬프트 생성 완료!" "Green"
Write-ColorText "[TIP] $OutputDir\review-prompt.txt를 Claude에게 복사하세요." "Yellow"