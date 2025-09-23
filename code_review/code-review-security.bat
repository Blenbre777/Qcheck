@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck 코드리뷰 시스템 - 보안 중심 리뷰
echo ===============================================
echo.

echo [SECURITY] 보안 취약점 중심으로 코드를 분석합니다...
echo.

echo 1단계: 코드 추출 중...
REM PowerShell 스크립트 실행 (변경분 기준)
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed

if %ERRORLEVEL% == 0 (
    echo.
    echo 2단계: 보안 중심 프롬프트 생성 중...

    REM 보안 템플릿 복사 및 코드 삽입
    if exist "review-output\templates\security-review-prompt.txt" (
        copy "review-output\templates\security-review-prompt.txt" "review-output\security-prompt.txt" >nul

        echo.
        echo [SUCCESS] 보안 중심 코드리뷰 프롬프트 생성 완료!
        echo.
        echo 📁 생성된 파일:
        echo    - review-output\security-prompt.txt ^(보안 중심 프롬프트 템플릿^)
        echo    - review-output\code-to-review.txt ^(분석할 코드^)
        echo    - review-output\file-list.txt ^(변경된 파일 목록^)
        echo.
        echo [SECURITY] 보안 검토 항목:
        echo    ✓ SQL 인젝션 취약점
        echo    ✓ XSS ^(Cross-Site Scripting^) 공격
        echo    ✓ 인증/권한 처리 로직
        echo    ✓ 민감정보 노출 방지
        echo    ✓ 입력값 검증 및 sanitization
        echo    ✓ 파일 업로드 보안
        echo    ✓ 세션 관리 보안
        echo.
        echo 📋 다음 단계:
        echo    1. review-output\security-prompt.txt 파일을 편집
        echo    2. {여기에 코드가 삽입됩니다} 부분을 실제 코드로 교체
        echo    3. Claude에게 보안 리뷰 요청
        echo.

        REM 보안 프롬프트 파일 자동 열기
        set /p openfile="[INFO] 보안 프롬프트 파일을 편집하시겠습니까? (y/n): "
        if /i "%openfile%"=="y" (
            echo [INFO] 보안 프롬프트 파일을 기본 편집기에서 열고 있습니다...
            start "" "review-output\security-prompt.txt"
            echo.
            echo 💡 편집 가이드:
            echo    - 파일 하단의 {여기에 코드가 삽입됩니다} 텍스트를 찾으세요
            echo    - 해당 부분을 review-output\code-to-review.txt의 내용으로 교체하세요
            echo    - 완성된 프롬프트를 Claude에게 전달하세요
        )
    ) else (
        echo [WARNING] 보안 템플릿 파일을 찾을 수 없습니다.
        echo 💡 scripts\create-prompt-templates.ps1을 먼저 실행해주세요.
    )
) else (
    echo.
    echo ❌ 코드 추출 중 오류가 발생했습니다.
    echo 💡 문제 해결 방법:
    echo    1. Git 저장소인지 확인
    echo    2. 변경된 파일이 있는지 확인
    echo    3. PowerShell 실행 정책 확인
    echo.
    echo 🔧 수동 실행 방법:
    echo    powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
)

echo.
pause