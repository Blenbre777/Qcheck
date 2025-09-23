# 🔍 Claude 자동 코드리뷰 시스템

Claude를 활용한 프롬프트 기반 자동 코드리뷰 시스템입니다.

## 🚀 빠른 시작

### 🐧 Linux/Mac 환경

#### 1. 변경분 코드리뷰
```bash
# 최근 변경된 코드만 리뷰
./scripts/extract-code.sh --changed
```

#### 2. 전체 코드리뷰
```bash
# 전체 코드베이스 리뷰
./scripts/extract-code.sh --all
```

#### 3. 특정 파일만 리뷰
```bash
# Java 파일만 리뷰
./scripts/extract-code.sh --files "*.java"

# TypeScript 파일만 리뷰
./scripts/extract-code.sh --files "*.ts"
```

### 🪟 Windows 환경

#### 🎯 간편 실행 (권장)
```cmd
REM 변경분 리뷰
code-review-changed.bat

REM 전체 코드 리뷰
code-review-all.bat

REM 보안 중심 리뷰
code-review-security.bat

REM 성능 최적화 리뷰
code-review-performance.bat

REM 초기 설정
setup-code-review.bat
```

#### 💻 PowerShell 직접 실행
```powershell
# 변경분 리뷰
.\scripts\extract-code.ps1 -Changed

# 전체 코드 리뷰
.\scripts\extract-code.ps1 -All

# 특정 커밋 이후 변경분
.\scripts\extract-code.ps1 -Since HEAD~3

# 특정 파일 패턴
.\scripts\extract-code.ps1 -Files "*.java"
```

#### 🔧 명령 프롬프트에서 실행
```cmd
REM PowerShell 스크립트 실행
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -All
```

## 📂 생성되는 파일들

실행 후 `review-output/` 디렉토리에 다음 파일들이 생성됩니다:

```
review-output/
├── code-to-review.txt        # 🔍 리뷰할 코드 (포맷팅됨)
├── review-prompt.txt         # 💬 Claude에게 보낼 완성된 프롬프트
├── file-list.txt            # 📋 변경된 파일 목록
├── diff-summary.txt         # 📊 Git diff 요약 (변경분 리뷰 시)
└── templates/               # 📝 특화 프롬프트 템플릿들
    ├── security-review-prompt.txt
    ├── performance-review-prompt.txt
    ├── architecture-review-prompt.txt
    ├── new-feature-review-prompt.txt
    └── usage-guide.md
```

## ⚙️ 설정 커스터마이징

`.code-review-config` 파일을 수정하여 프로젝트에 맞게 설정을 변경할 수 있습니다:

```bash
# 리뷰 대상 디렉토리
TARGET_DIRS="back/src front/src"

# 제외할 패턴
EXCLUDE_PATTERNS="node_modules target .git *.min.js *.map"

# 포함할 파일 확장자
INCLUDE_EXTENSIONS="java ts js html scss css xml properties"

# 최대 파일 크기 (KB)
MAX_FILE_SIZE=500
```

## 🎯 특화 프롬프트 템플릿

상황에 맞는 전문 프롬프트를 활용하세요:

### 🔒 보안 중심 리뷰
```bash
cp review-output/templates/security-review-prompt.txt review-output/custom-prompt.txt
# 코드 삽입 후 Claude에게 전달
```

### ⚡ 성능 최적화 리뷰
```bash
cp review-output/templates/performance-review-prompt.txt review-output/custom-prompt.txt
# 성능 병목지점 분석에 특화
```

### 🏗️ 아키텍처 리뷰
```bash
cp review-output/templates/architecture-review-prompt.txt review-output/custom-prompt.txt
# 설계 구조 전반적 검토
```

### 🆕 신규 기능 리뷰
```bash
cp review-output/templates/new-feature-review-prompt.txt review-output/custom-prompt.txt
# 새로운 기능 종합 검토
```

## 📋 사용 워크플로우

### 1. 코드 추출 및 프롬프트 생성
```bash
./scripts/extract-code.sh --changed
```

### 2. Claude에게 리뷰 요청
- `review-output/review-prompt.txt` 내용을 복사
- Claude에게 붙여넣기하여 리뷰 요청

### 3. 리뷰 결과 저장
```bash
# Claude의 응답을 저장
# review-output/claude-response.txt 파일에 저장 권장
```

### 4. 개선사항 적용 후 재검토
```bash
# 코드 수정 후 다시 리뷰
./scripts/extract-code.sh --changed
```

## 🎨 고급 사용법

### 특정 커밋 이후 변경분 리뷰
```bash
./scripts/extract-code.sh --since HEAD~3
./scripts/extract-code.sh --since abc1234
```

### 다중 패턴 파일 리뷰
```bash
# Java 파일과 설정 파일만
find back/src -name "*.java" -o -name "*.xml" -o -name "*.properties" | \
xargs ./scripts/extract-code.sh --files
```

### 대용량 프로젝트 분할 리뷰
```bash
# 백엔드만
TARGET_DIRS="back/src" ./scripts/extract-code.sh --all

# 프론트엔드만
TARGET_DIRS="front/src" ./scripts/extract-code.sh --all
```

## 🔧 문제 해결

### 📁 파일이 너무 클 때

#### Linux/Mac
```bash
# 최대 파일 크기 조정 (KB 단위)
echo "MAX_FILE_SIZE=1000" >> .code-review-config
```

#### Windows
```cmd
REM 설정 파일 수정
echo MAX_FILE_SIZE=1000 >> .code-review-config
```

### 🚫 특정 파일 제외

#### Linux/Mac
```bash
# 제외 패턴 추가
echo "EXCLUDE_PATTERNS=\"node_modules target .git *.min.js *.map test-data\"" >> .code-review-config
```

#### Windows
```cmd
REM 제외 패턴 추가
echo EXCLUDE_PATTERNS="node_modules target .git *.min.js *.map test-data" >> .code-review-config
```

### 📊 Git 저장소가 아닌 경우

#### Linux/Mac
```bash
# 전체 코드 리뷰만 사용
./scripts/extract-code.sh --all
```

#### Windows
```cmd
REM 전체 코드 리뷰만 사용
code-review-all.bat
```

### 🛡️ PowerShell 실행 정책 오류 (Windows 전용)

#### 문제: "이 시스템에서 스크립트를 실행할 수 없습니다"

#### 해결방법 1: 실행 정책 변경 (권장)
```powershell
# 현재 사용자에 대해서만 변경
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### 해결방법 2: 일회성 실행
```cmd
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
```

#### 해결방법 3: 자동 설정
```cmd
REM 초기 설정 스크립트 실행
setup-code-review.bat
```

### 🔍 Git 명령어 오류 (Windows)

#### 문제: "git is not recognized"

#### 해결방법:
1. **Git for Windows 설치**
   - https://git-scm.com/download/win 에서 다운로드
   - 설치 시 "Git from the command line" 옵션 선택

2. **환경변수 확인**
   ```cmd
   REM Git 설치 확인
   git --version

   REM PATH 환경변수에 Git 경로 추가 (보통 자동으로 됨)
   REM C:\Program Files\Git\bin
   ```

### 📝 파일 인코딩 문제 (Windows)

#### 문제: 한글이 깨져서 표시됨

#### 해결방법:
```cmd
REM 콘솔 인코딩을 UTF-8로 변경
chcp 65001

REM 또는 배치 파일에서 자동으로 설정됨
```

### 🔒 파일 권한 오류 (Windows)

#### 문제: "액세스가 거부되었습니다"

#### 해결방법:
1. **관리자 권한으로 실행**
   - 명령 프롬프트를 관리자 권한으로 실행
   - 배치 파일을 우클릭 > "관리자 권한으로 실행"

2. **사용자 디렉토리에서 실행**
   ```cmd
   REM 사용자 홈 디렉토리로 이동
   cd %USERPROFILE%\Desktop\project\Qcheck
   ```

### 💾 대용량 출력 파일 문제

#### Windows에서 메모리 부족 시:
```cmd
REM 파일을 부분별로 나누어 처리
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Files "*.java"
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Files "*.ts"
```

## 🎯 권장 리뷰 시나리오

### 🔄 일상적 개발 사이클
1. **기능 개발 중**: `--changed` 옵션으로 변경분 리뷰
2. **Pull Request 전**: 신규 기능 템플릿 활용
3. **릴리즈 전**: 보안 + 성능 템플릿 조합 리뷰

### 🎨 특별한 상황
1. **보안 취약점 의심**: 보안 중심 템플릿
2. **성능 이슈 발생**: 성능 최적화 템플릿
3. **리팩토링 계획**: 아키텍처 템플릿
4. **신규 팀원 온보딩**: 전체 코드 + 기본 템플릿

## 📊 리뷰 품질 향상 팁

### 🎯 효과적인 프롬프트 작성
- **구체적인 요구사항** 명시
- **우선순위** 설정 (보안 > 성능 > 가독성)
- **컨텍스트 정보** 충분히 제공

### 📋 결과 활용
- **우선순위별 액션 아이템** 정리
- **팀 공유** 및 학습 자료 활용
- **지속적 개선** 프로세스에 통합

## 🤝 기여 및 피드백

이 시스템을 개선하거나 새로운 템플릿을 추가하고 싶다면:

1. `.code-review-config` 파일 커스터마이징
2. `scripts/create-prompt-templates.sh`에 새 템플릿 추가
3. 프로젝트팀과 베스트 프랙티스 공유

## 📋 플랫폼별 지원 현황

| 기능 | Linux/Mac | Windows PowerShell | Windows CMD | 비고 |
|------|-----------|-------------------|-------------|------|
| 코드 추출 | ✅ | ✅ | ✅ | 배치 파일 지원 |
| 프롬프트 생성 | ✅ | ✅ | ✅ | UTF-8 인코딩 |
| Git 연동 | ✅ | ✅ | ✅ | Git for Windows 필요 |
| 템플릿 시스템 | ✅ | ✅ | ✅ | 동일한 템플릿 |
| 한글 지원 | ✅ | ✅ | ✅ | UTF-8 인코딩 |
| 색상 출력 | ✅ | ✅ | ⚠️ | CMD는 제한적 |
| 대화형 설정 | ✅ | ✅ | ✅ | 초기 설정 스크립트 |

## 🎉 Windows 전용 추가 기능

### 📂 Windows 탐색기 통합
- 프로젝트 폴더에서 우클릭 → "PowerShell에서 열기"
- 배치 파일 더블클릭으로 간편 실행

### 🎯 원클릭 실행 배치 파일
- `code-review-changed.bat`: 변경분 리뷰
- `code-review-all.bat`: 전체 코드 리뷰
- `code-review-security.bat`: 보안 중심 리뷰
- `code-review-performance.bat`: 성능 최적화 리뷰
- `setup-code-review.bat`: 초기 설정 자동화

### 📝 자동 파일 열기
- 생성된 프롬프트 파일을 기본 편집기에서 자동 열기
- VS Code, 메모장 등 기본 프로그램과 연동

### 🔧 PowerShell 실행 정책 자동 설정
- 초기 설정 스크립트에서 실행 정책 자동 확인 및 설정
- 사용자 동의 하에 안전한 정책으로 변경

---

**💡 Tip**:
- **Linux/Mac 사용자**: 쉘 스크립트(`.sh`)를 사용하세요
- **Windows 사용자**: 배치 파일(`.bat`)을 더블클릭하여 간편하게 실행하세요
- **개발팀**: 두 플랫폼 모두 동일한 결과를 제공하므로 협업에 최적화되어 있습니다

정기적으로 코드리뷰를 수행하여 코드 품질을 지속적으로 향상시키세요! 🚀