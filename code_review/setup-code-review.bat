@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck 코드리뷰 시스템 - 초기 설정
echo ===============================================
echo.

echo 🚀 코드리뷰 시스템을 초기 설정합니다...
echo.

REM 1. 디렉토리 생성
echo 1단계: 필요한 디렉토리 생성 중...
if not exist "scripts" mkdir scripts
if not exist "review-output" mkdir review-output
if not exist "review-output\templates" mkdir review-output\templates
echo [SUCCESS] 디렉토리 생성 완료

REM 2. PowerShell 실행 정책 확인
echo.
echo 2단계: PowerShell 실행 정책 확인 중...
powershell -Command "Get-ExecutionPolicy" >temp_policy.txt
set /p current_policy=<temp_policy.txt
del temp_policy.txt

echo 현재 PowerShell 실행 정책: %current_policy%

if "%current_policy%"=="Restricted" (
    echo.
    echo [WARNING] PowerShell 실행 정책이 제한되어 있습니다.
    echo 💡 다음 중 하나를 선택하세요:
    echo.
    echo    1. 현재 사용자에 대해서만 실행 정책 변경 ^(권장^)
    echo    2. 일회성으로 스크립트 실행
    echo    3. 설정하지 않고 계속
    echo.
    set /p policy_choice="선택 (1/2/3): "

    if "%policy_choice%"=="1" (
        echo 사용자 실행 정책을 RemoteSigned로 변경합니다...
        powershell -Command "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force"
        echo [SUCCESS] 실행 정책 변경 완료
    ) else if "%policy_choice%"=="2" (
        echo 💡 스크립트 실행 시 다음 명령어를 사용하세요:
        echo    powershell -ExecutionPolicy Bypass -File "scripts\extract-code.ps1" -Changed
    ) else (
        echo [WARNING] 실행 정책 미변경. 스크립트 실행 시 오류가 발생할 수 있습니다.
    )
) else (
    echo [SUCCESS] PowerShell 실행 정책이 적절히 설정되어 있습니다.
)

REM 3. Git 설치 확인
echo.
echo 3단계: Git 설치 확인 중...
git --version >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo [SUCCESS] Git이 설치되어 있습니다.
    git --version
) else (
    echo ❌ Git이 설치되어 있지 않습니다.
    echo 💡 Git for Windows를 설치해주세요: https://git-scm.com/download/win
)

REM 4. 프롬프트 템플릿 생성
echo.
echo 4단계: 프롬프트 템플릿 생성 중...
if exist "scripts\create-prompt-templates.ps1" (
    powershell -ExecutionPolicy Bypass -File "scripts\create-prompt-templates.ps1"
    echo [SUCCESS] 프롬프트 템플릿 생성 완료
) else (
    echo [WARNING] create-prompt-templates.ps1 파일이 없습니다.
    echo 💡 스크립트 파일들이 모두 있는지 확인해주세요.
)

REM 5. 설정 파일 확인
echo.
echo 5단계: 설정 파일 확인 중...
if exist ".code-review-config" (
    echo [SUCCESS] 설정 파일이 존재합니다.
    echo 📄 현재 설정:
    findstr /R "^[^#]" .code-review-config
) else (
    echo [WARNING] 설정 파일이 없습니다. 기본값을 사용합니다.
    echo 💡 .code-review-config 파일을 생성하여 설정을 커스터마이징할 수 있습니다.
)

REM 6. 테스트 실행
echo.
echo 6단계: 시스템 테스트 중...
echo [INFO] 간단한 테스트를 실행합니다...

if exist "scripts\extract-code.ps1" (
    echo [SUCCESS] extract-code.ps1 스크립트 존재
) else (
    echo ❌ extract-code.ps1 스크립트 없음
)

if exist "scripts\generate-prompt.ps1" (
    echo [SUCCESS] generate-prompt.ps1 스크립트 존재
) else (
    echo ❌ generate-prompt.ps1 스크립트 없음
)

echo.
echo ===============================================
echo           🎉 초기 설정 완료!
echo ===============================================
echo.
echo 📋 사용 가능한 명령어:
echo.
echo    🔄 변경분 리뷰:     code-review-changed.bat
echo    [ALL] 전체 코드 리뷰:   code-review-all.bat
echo    [SECURITY] 보안 중심 리뷰:   code-review-security.bat
echo    [PERFORMANCE] 성능 최적화 리뷰: code-review-performance.bat
echo.
echo 📁 주요 파일 위치:
echo    • 스크립트:        scripts\
echo    • 결과 출력:       review-output\
echo    • 템플릿:          review-output\templates\
echo    • 설정 파일:       .code-review-config
echo.
echo 📖 자세한 사용법은 README-code-review.md 파일을 참조하세요.
echo.

pause