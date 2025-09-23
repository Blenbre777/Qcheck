@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck 코드리뷰 시스템 - 전체 코드 리뷰
echo ===============================================
echo.

echo [INFO] 전체 코드베이스를 추출하여 Claude 리뷰용 프롬프트를 생성합니다...
echo [WARNING] 파일이 많을 경우 시간이 소요될 수 있습니다.
echo.

REM PowerShell 스크립트 실행
powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -All

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] 전체 코드 추출 및 프롬프트 생성 완료!
    echo.
    echo 📁 결과 파일:
    echo    - review-output\review-prompt.txt ^(Claude에게 복사할 프롬프트^)
    echo    - review-output\code-to-review.txt ^(추출된 전체 코드^)
    echo    - review-output\file-list.txt ^(포함된 파일 목록^)
    echo.
    echo 📋 다음 단계:
    echo    1. review-output\review-prompt.txt 파일을 열어서 내용을 복사
    echo    2. Claude에게 붙여넣기하여 전체 코드리뷰 요청
    echo    3. 리뷰 결과를 review-output\claude-response.txt에 저장
    echo.
    echo 💡 팁: 전체 코드가 클 경우 부분별로 나누어 리뷰하는 것을 권장합니다.
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
    echo    1. 대상 디렉토리^(back/src, front/src^)가 있는지 확인
    echo    2. 파일 크기 제한 설정 확인 ^(.code-review-config^)
    echo    3. PowerShell 실행 정책 확인
    echo.
)

echo.
pause