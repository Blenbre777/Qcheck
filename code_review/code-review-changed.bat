@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck 코드리뷰 시스템 - 변경분 리뷰
echo ===============================================
echo.

echo [INFO] 최근 변경된 코드를 추출하여 Claude 리뷰용 프롬프트를 생성합니다...
echo.

REM PowerShell 스크립트 실행
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] 코드 추출 및 프롬프트 생성 완료!
    echo.
    echo 📁 결과 파일:
    echo    - review-output\review-prompt.txt ^(Claude에게 복사할 프롬프트^)
    echo    - review-output\code-to-review.txt ^(추출된 코드^)
    echo    - review-output\file-list.txt ^(변경된 파일 목록^)
    echo    - review-output\diff-summary.txt ^(Git diff 요약^)
    echo.
    echo 📋 다음 단계:
    echo    1. review-output\review-prompt.txt 파일을 열어서 내용을 복사
    echo    2. Claude에게 붙여넣기하여 코드리뷰 요청
    echo    3. 리뷰 결과를 review-output\claude-response.txt에 저장
    echo.

    REM 자동으로 결과 파일 열기 (선택사항)
    set /p openfile="[INFO] 결과 파일을 자동으로 열까요? (y/n): "
    if /i "%openfile%"=="y" (
        if exist "review-output\review-prompt.txt" (
            echo [INFO] 프롬프트 파일을 기본 편집기에서 열고 있습니다...
            start "" "review-output\review-prompt.txt"
        )
    )
) else (
    echo.
    echo ❌ 코드 추출 중 오류가 발생했습니다.
    echo 💡 문제 해결 방법:
    echo    1. Git 저장소인지 확인
    echo    2. 변경된 파일이 있는지 확인
    echo    3. PowerShell 실행 정책 확인
    echo.
)

echo.
pause