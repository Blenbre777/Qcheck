#!/bin/bash

# =============================================================================
# 프롬프트 생성 스크립트 - Claude 코드리뷰용
# =============================================================================
# 코드 추출된 내용을 바탕으로 Claude에게 보낼 프롬프트를 생성합니다.
# =============================================================================

# UTF-8 인코딩 설정
export LC_ALL=C.UTF-8
export LANG=C.UTF-8

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 설정 파일 로드
CONFIG_FILE=".code-review-config"
if [[ -f "$CONFIG_FILE" ]]; then
    source "$CONFIG_FILE"
else
    OUTPUT_DIR="review-output"
fi

MODE="$1"

# 함수: 기본 프롬프트 템플릿 생성
generate_basic_prompt() {
    local prompt_file="$OUTPUT_DIR/review-prompt.txt"
    local code_file="$OUTPUT_DIR/code-to-review.txt"
    local file_list="$OUTPUT_DIR/file-list.txt"

    # 파일이 존재하는지 확인
    if [[ ! -f "$code_file" ]] || [[ ! -f "$file_list" ]]; then
        echo -e "${RED}❌ 필요한 파일이 없습니다: $code_file 또는 $file_list${NC}"
        exit 1
    fi

    # 파일 통계
    local file_count=$(wc -l < "$file_list")
    local line_count=$(wc -l < "$code_file")

    cat > "$prompt_file" << 'EOF'
# 프로젝트 정보
- **프로젝트명**: Qcheck (질문 체크 시스템)
- **백엔드**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate
- **프론트엔드**: Angular 20, TypeScript, SCSS
- **아키텍처**: RESTful API, SPA (Single Page Application)

# 코드리뷰 요청

다음 코드에 대해 전문적이고 상세한 코드리뷰를 수행해주세요.

## 🎯 리뷰 관점

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

## 📊 코드 통계
EOF

    # 통계 정보 추가
    echo "- **총 파일 수**: ${file_count}개" >> "$prompt_file"
    echo "- **총 라인 수**: ${line_count}줄" >> "$prompt_file"
    echo "" >> "$prompt_file"

    echo "## 📁 파일 목록" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    cat "$file_list" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    echo "" >> "$prompt_file"

    echo "## [CODE] 코드 내용" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    cat "$code_file" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    echo "" >> "$prompt_file"

    cat >> "$prompt_file" << 'EOF'
## [REQUEST] 요청사항

### 1. **이슈 분석 및 분류**
각 발견된 이슈에 대해 다음과 같이 분류해주세요:

- **🔴 Critical**: 보안 취약점, 심각한 버그 가능성
- **🟠 High**: 성능 이슈, 아키텍처 문제
- **🟡 Medium**: 코드 품질, 유지보수성 개선
- **🟢 Low**: 코딩 컨벤션, 스타일 가이드

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

**💡 참고**: 단순한 문제 지적보다는 **교육적 가치**가 있는 리뷰를 부탁드립니다. 각 제안사항이 **왜 중요한지**, **어떤 이점을 가져다주는지** 설명해주시면 더욱 도움이 됩니다.
EOF

    echo -e "${GREEN}[SUCCESS] 기본 프롬프트 생성 완료: $prompt_file${NC}"
}

# 함수: 변경분 전용 프롬프트 생성
generate_changed_prompt() {
    local prompt_file="$OUTPUT_DIR/review-prompt.txt"
    local code_file="$OUTPUT_DIR/code-to-review.txt"
    local file_list="$OUTPUT_DIR/file-list.txt"
    local diff_file="$OUTPUT_DIR/diff-summary.txt"

    # Git diff 정보가 있는지 확인
    if [[ ! -f "$diff_file" ]]; then
        echo -e "${YELLOW}[WARNING] Git diff 파일이 없어 기본 프롬프트로 생성합니다.${NC}"
        generate_basic_prompt
        return
    fi

    # 파일 통계
    local file_count=$(wc -l < "$file_list")
    local line_count=$(wc -l < "$code_file")

    # Git diff 통계 추출
    local diff_stats=$(head -20 "$diff_file" | grep -E "files? changed|insertions?|deletions?" | head -1)

    cat > "$prompt_file" << 'EOF'
# 변경분 코드리뷰 요청

## 🔄 프로젝트 정보
- **프로젝트명**: Qcheck (질문 체크 시스템)
- **백엔드**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate
- **프론트엔드**: Angular 20, TypeScript, SCSS
- **아키텍처**: RESTful API, SPA (Single Page Application)

## 📈 변경 요약
EOF

    echo "- **변경된 파일**: ${file_count}개" >> "$prompt_file"
    echo "- **총 라인 수**: ${line_count}줄" >> "$prompt_file"
    if [[ -n "$diff_stats" ]]; then
        echo "- **Git 통계**: $diff_stats" >> "$prompt_file"
    fi
    echo "" >> "$prompt_file"

    echo "## 📊 Git Diff 요약" >> "$prompt_file"
    echo '```diff' >> "$prompt_file"
    head -50 "$diff_file" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    echo "" >> "$prompt_file"

    echo "## 📁 변경된 파일 목록" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    cat "$file_list" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    echo "" >> "$prompt_file"

    echo "## [CODE] 변경된 파일별 전체 코드" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    cat "$code_file" >> "$prompt_file"
    echo '```' >> "$prompt_file"
    echo "" >> "$prompt_file"

    cat >> "$prompt_file" << 'EOF'
## 🎯 변경분 리뷰 요청사항

### 1. **변경 영향도 분석**
- 이번 변경이 **기존 코드에 미치는 영향** 분석
- **연관된 컴포넌트나 모듈**에 대한 영향도 평가
- **데이터베이스 스키마나 API 변경**으로 인한 호환성 이슈

### 2. **변경사항 품질 검토**
- 새로 추가된 코드의 **품질 평가**
- 수정된 로직의 **정확성 및 효율성**
- **코딩 표준 및 컨벤션** 준수 여부

### 3. **리스크 평가**
- **🔴 High Risk**: 즉시 수정이 필요한 심각한 문제
- **🟠 Medium Risk**: 주의 깊게 모니터링이 필요한 부분
- **🟢 Low Risk**: 개선하면 좋을 부분

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

## 📋 우선순위별 액션 아이템

리뷰 결과를 다음과 같이 정리해주세요:

### 🚨 즉시 수정 필요 (Critical)
- [ ] 수정해야 할 심각한 문제들

### [HIGH] 배포 전 수정 권장
- [ ] 배포하기 전에 개선하면 좋을 사항들

### 💡 향후 개선 사항 (Medium/Low)
- [ ] 시간이 날 때 개선할 수 있는 사항들

---

**🎯 목표**: 안전하고 품질 높은 코드 변경을 통한 시스템 개선
EOF

    echo -e "${GREEN}[SUCCESS] 변경분 전용 프롬프트 생성 완료: $prompt_file${NC}"
}

# 메인 함수
main() {
    echo -e "${BLUE}[INFO] Claude 코드리뷰 프롬프트 생성${NC}"

    case "$MODE" in
        "changed")
            generate_changed_prompt
            ;;
        "all"|"pattern"|*)
            generate_basic_prompt
            ;;
    esac

    # 추가 템플릿 파일들도 생성
    ./scripts/create-prompt-templates.sh 2>/dev/null || true

    echo -e "${GREEN}[SUCCESS] 프롬프트 생성 완료!${NC}"
    echo -e "${YELLOW}💡 $OUTPUT_DIR/review-prompt.txt를 Claude에게 복사하세요.${NC}"
}

# 스크립트 실행
main "$@"