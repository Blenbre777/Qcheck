#!/bin/bash

# =============================================================================
# 코드 추출 스크립트 - Claude 코드리뷰용
# =============================================================================
# 사용법:
#   ./scripts/extract-code.sh --all          # 전체 코드 추출
#   ./scripts/extract-code.sh --changed      # 변경분만 추출
#   ./scripts/extract-code.sh --since HEAD~3 # 특정 커밋 이후 변경분
#   ./scripts/extract-code.sh --files "*.java" # 특정 패턴 파일만
# =============================================================================

# UTF-8 인코딩 설정
export LC_ALL=C.UTF-8
export LANG=C.UTF-8

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정 파일 로드
CONFIG_FILE=".code-review-config"
if [[ -f "$CONFIG_FILE" ]]; then
    source "$CONFIG_FILE"
else
    echo -e "${YELLOW}[WARNING] 설정 파일이 없습니다. 기본값을 사용합니다.${NC}"
    # 기본 설정값
    TARGET_DIRS="back/src front/src"
    EXCLUDE_PATTERNS="node_modules target .git *.min.js *.map"
    INCLUDE_EXTENSIONS="java ts js html scss css xml properties"
    MAX_FILE_SIZE=500
    OUTPUT_DIR="review-output"
fi

# 출력 디렉토리 생성
mkdir -p "$OUTPUT_DIR"

# 변수 초기화
MODE=""
SINCE_COMMIT=""
FILE_PATTERN=""

# 함수: 도움말 출력
show_help() {
    echo "코드리뷰용 코드 추출 스크립트"
    echo ""
    echo "사용법:"
    echo "  $0 --all                    전체 코드 추출"
    echo "  $0 --changed                최근 변경분 추출 (HEAD~1과 비교)"
    echo "  $0 --since <commit>         특정 커밋 이후 변경분 추출"
    echo "  $0 --files <pattern>        특정 파일 패턴만 추출"
    echo "  $0 --help                   이 도움말 출력"
    echo ""
    echo "예시:"
    echo "  $0 --changed"
    echo "  $0 --since HEAD~3"
    echo "  $0 --files \"*.java\""
    echo "  $0 --all"
}

# 함수: 파일 크기 체크 (KB 단위)
check_file_size() {
    local file="$1"
    if [[ -f "$file" ]]; then
        local size_kb=$(du -k "$file" | cut -f1)
        if [[ $size_kb -gt $MAX_FILE_SIZE ]]; then
            echo -e "${YELLOW}[WARNING] 큰 파일 건너뜀: $file (${size_kb}KB > ${MAX_FILE_SIZE}KB)${NC}"
            return 1
        fi
    fi
    return 0
}

# 함수: 파일 확장자 체크
is_target_file() {
    local file="$1"
    local ext="${file##*.}"

    # 확장자가 포함 목록에 있는지 확인
    for include_ext in $INCLUDE_EXTENSIONS; do
        if [[ "$ext" == "$include_ext" ]]; then
            return 0
        fi
    done
    return 1
}

# 함수: 제외 패턴 체크
is_excluded() {
    local file="$1"

    for pattern in $EXCLUDE_PATTERNS; do
        if [[ "$file" == *"$pattern"* ]]; then
            return 0
        fi
    done
    return 1
}

# 함수: 전체 코드 추출
extract_all_code() {
    echo -e "${BLUE}[INFO] 전체 코드 추출 시작${NC}"

    local code_file="$OUTPUT_DIR/code-to-review.txt"
    local file_list="$OUTPUT_DIR/file-list.txt"

    # 파일 초기화
    > "$code_file"
    > "$file_list"

    local file_count=0
    local line_count=0

    # 대상 디렉토리에서 파일 찾기
    for dir in $TARGET_DIRS; do
        if [[ -d "$dir" ]]; then
            echo -e "${BLUE}📁 $dir 디렉토리 스캔 중...${NC}"

            while IFS= read -r -d '' file; do
                # 제외 패턴 체크
                if is_excluded "$file"; then
                    continue
                fi

                # 대상 파일인지 체크
                if ! is_target_file "$file"; then
                    continue
                fi

                # 파일 크기 체크
                if ! check_file_size "$file"; then
                    continue
                fi

                echo "$file" >> "$file_list"
                echo "=== $file ===" >> "$code_file"
                cat "$file" >> "$code_file"
                echo -e "\n\n" >> "$code_file"

                local lines=$(wc -l < "$file")
                ((file_count++))
                ((line_count += lines))

                echo -e "  [OK] $file (${lines} lines)"

            done < <(find "$dir" -type f -print0 2>/dev/null)
        else
            echo -e "${YELLOW}[WARNING] 디렉토리가 없습니다: $dir${NC}"
        fi
    done

    echo -e "${GREEN}[SUCCESS] 전체 코드 추출 완료: ${file_count}개 파일, ${line_count}줄${NC}"
    return $file_count
}

# 함수: 변경분 추출
extract_changed_code() {
    local since="${1:-HEAD~1}"
    echo -e "${BLUE}[INFO] 변경분 추출 시작 (기준: $since)${NC}"

    # Git 저장소 확인
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo -e "${RED}❌ Git 저장소가 아닙니다.${NC}"
        exit 1
    fi

    local code_file="$OUTPUT_DIR/code-to-review.txt"
    local file_list="$OUTPUT_DIR/file-list.txt"
    local diff_file="$OUTPUT_DIR/diff-summary.txt"

    # Git diff 정보 생성
    echo -e "${BLUE}📊 Git diff 정보 생성 중...${NC}"
    git diff --stat "$since" > "$diff_file"
    git diff "$since" >> "$diff_file"

    # 변경된 파일 목록 가져오기
    git diff --name-only "$since" > "$file_list"

    if [[ ! -s "$file_list" ]]; then
        echo -e "${YELLOW}[WARNING] 변경된 파일이 없습니다.${NC}"
        return 0
    fi

    # 파일 초기화
    > "$code_file"

    local file_count=0
    local line_count=0

    # 변경된 파일별로 전체 내용 추출
    while IFS= read -r file; do
        # 파일이 존재하는지 확인 (삭제된 파일일 수 있음)
        if [[ ! -f "$file" ]]; then
            echo -e "  [DELETED] 삭제된 파일: $file"
            continue
        fi

        # 제외 패턴 체크
        if is_excluded "$file"; then
            continue
        fi

        # 대상 파일인지 체크
        if ! is_target_file "$file"; then
            continue
        fi

        # 파일 크기 체크
        if ! check_file_size "$file"; then
            continue
        fi

        echo "=== $file ===" >> "$code_file"
        cat "$file" >> "$code_file"
        echo -e "\n\n" >> "$code_file"

        local lines=$(wc -l < "$file")
        ((file_count++))
        ((line_count += lines))

        echo -e "  [OK] $file (${lines} lines)"

    done < "$file_list"

    echo -e "${GREEN}[SUCCESS] 변경분 추출 완료: ${file_count}개 파일, ${line_count}줄${NC}"
    return $file_count
}

# 함수: 특정 파일 패턴 추출
extract_pattern_files() {
    local pattern="$1"
    echo -e "${BLUE}🔍 패턴별 파일 추출: $pattern${NC}"

    local code_file="$OUTPUT_DIR/code-to-review.txt"
    local file_list="$OUTPUT_DIR/file-list.txt"

    # 파일 초기화
    > "$code_file"
    > "$file_list"

    local file_count=0
    local line_count=0

    # 패턴에 맞는 파일 찾기
    for dir in $TARGET_DIRS; do
        if [[ -d "$dir" ]]; then
            while IFS= read -r -d '' file; do
                # 제외 패턴 체크
                if is_excluded "$file"; then
                    continue
                fi

                # 파일 크기 체크
                if ! check_file_size "$file"; then
                    continue
                fi

                echo "$file" >> "$file_list"
                echo "=== $file ===" >> "$code_file"
                cat "$file" >> "$code_file"
                echo -e "\n\n" >> "$code_file"

                local lines=$(wc -l < "$file")
                ((file_count++))
                ((line_count += lines))

                echo -e "  [OK] $file (${lines} lines)"

            done < <(find "$dir" -name "$pattern" -type f -print0 2>/dev/null)
        fi
    done

    echo -e "${GREEN}[SUCCESS] 패턴 파일 추출 완료: ${file_count}개 파일, ${line_count}줄${NC}"
    return $file_count
}

# 메인 로직
main() {
    echo -e "${GREEN}🚀 Claude 코드리뷰용 코드 추출 스크립트${NC}"
    echo "================================================"

    # 파라미터 파싱
    while [[ $# -gt 0 ]]; do
        case $1 in
            --all)
                MODE="all"
                shift
                ;;
            --changed)
                MODE="changed"
                shift
                ;;
            --since)
                MODE="changed"
                SINCE_COMMIT="$2"
                shift 2
                ;;
            --files)
                MODE="pattern"
                FILE_PATTERN="$2"
                shift 2
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                echo -e "${RED}❌ 알 수 없는 옵션: $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done

    # 모드가 지정되지 않은 경우
    if [[ -z "$MODE" ]]; then
        echo -e "${RED}❌ 옵션을 지정해주세요.${NC}"
        show_help
        exit 1
    fi

    # 모드별 실행
    case $MODE in
        "all")
            extract_all_code
            file_count=$?
            ;;
        "changed")
            if [[ -n "$SINCE_COMMIT" ]]; then
                extract_changed_code "$SINCE_COMMIT"
            else
                extract_changed_code
            fi
            file_count=$?
            ;;
        "pattern")
            extract_pattern_files "$FILE_PATTERN"
            file_count=$?
            ;;
    esac

    # 프롬프트 생성
    if [[ $file_count -gt 0 ]]; then
        echo -e "${BLUE}[INFO] 프롬프트 생성 중...${NC}"
        ./scripts/generate-prompt.sh "$MODE"

        echo ""
        echo -e "${GREEN}[SUCCESS] 코드 추출 완료!${NC}"
        echo ""
        echo -e "${BLUE}📁 출력 파일:${NC}"
        echo -e "  - $OUTPUT_DIR/code-to-review.txt (${file_count} files)"
        echo -e "  - $OUTPUT_DIR/review-prompt.txt (완성된 프롬프트)"
        echo -e "  - $OUTPUT_DIR/file-list.txt (파일 목록)"
        if [[ "$MODE" == "changed" ]]; then
            echo -e "  - $OUTPUT_DIR/diff-summary.txt (Git diff 요약)"
        fi
        echo ""
        echo -e "${YELLOW}📋 다음 단계:${NC}"
        echo -e "  1. $OUTPUT_DIR/review-prompt.txt 내용을 Claude에게 복사"
        echo -e "  2. 리뷰 결과를 $OUTPUT_DIR/claude-response.txt에 저장"
        echo -e "  3. 개선사항 적용 후 재검토"

    else
        echo -e "${YELLOW}[WARNING] 추출된 파일이 없습니다.${NC}"
    fi
}

# 스크립트 실행
main "$@"