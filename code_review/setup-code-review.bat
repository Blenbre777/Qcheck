@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck Code Review System - Initial Setup
echo ===============================================
echo.

echo 🚀 Setting up the code review system...
echo.

REM 1. Directory creation
echo Step 1: Creating required directories...
if not exist "scripts" mkdir scripts
if not exist "code_review\review-output" mkdir code_review\review-output
if not exist "code_review\review-output\templates" mkdir code_review\review-output\templates
echo [SUCCESS] Directory creation completed

REM 2. PowerShell execution policy check
echo.
echo Step 2: Checking PowerShell execution policy...
powershell -Command "Get-ExecutionPolicy" >temp_policy.txt
set /p current_policy=<temp_policy.txt
del temp_policy.txt

echo Current PowerShell execution policy: %current_policy%

if "%current_policy%"=="Restricted" (
    echo.
    echo [WARNING] PowerShell execution policy is restricted.
    echo 💡 Please choose one of the following options:
    echo.
    echo    1. Change execution policy for current user only ^(Recommended^)
    echo    2. Execute scripts one-time with bypass
    echo    3. Continue without changing settings
    echo.
    set /p policy_choice="Choice (1/2/3): "

    if "%policy_choice%"=="1" (
        echo Changing user execution policy to RemoteSigned...
        powershell -Command "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force"
        echo [SUCCESS] Execution policy changed successfully
    ) else if "%policy_choice%"=="2" (
        echo 💡 Use the following command when running scripts:
        echo    powershell -ExecutionPolicy Bypass -Command "& '.\code_review\scripts\extract-code.ps1' -Changed"
    ) else (
        echo [WARNING] Execution policy unchanged. Script execution errors may occur.
    )
) else (
    echo [SUCCESS] PowerShell execution policy is properly configured.
)

REM 3. Git installation check
echo.
echo Step 3: Checking Git installation...
git --version >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo [SUCCESS] Git is installed.
    git --version
) else (
    echo ❌ Git is not installed.
    echo 💡 Please install Git for Windows: https://git-scm.com/download/win
)

REM 4. Generate prompt templates
echo.
echo Step 4: Generating prompt templates...
if exist "scripts\create-prompt-templates.ps1" (
    powershell -ExecutionPolicy Bypass -Command "& '.\code_review\scripts\create-prompt-templates.ps1'"
    echo [SUCCESS] Prompt template generation completed
) else (
    echo [WARNING] create-prompt-templates.ps1 file not found.
    echo 💡 Please ensure all script files are present.
)

REM 5. Configuration file check
echo.
echo Step 5: Checking configuration file...
if exist ".code-review-config" (
    echo [SUCCESS] Configuration file exists.
    echo 📄 Current Settings:
    findstr /R "^[^#]" .code-review-config
) else (
    echo [WARNING] Configuration file not found. Using default values.
    echo 💡 You can create a .code-review-config file to customize settings.
)

REM 6. System test
echo.
echo Step 6: Testing system...
echo [INFO] Running simple system test...

if exist "scripts\extract-code.ps1" (
    echo [SUCCESS] extract-code.ps1 script exists
) else (
    echo ❌ extract-code.ps1 script missing
)

if exist "scripts\generate-prompt.ps1" (
    echo [SUCCESS] generate-prompt.ps1 script exists
) else (
    echo ❌ generate-prompt.ps1 script missing
)

echo.
echo ===============================================
echo           🎉 Initial Setup Complete!
echo ===============================================
echo.
echo 📋 Available Commands:
echo.
echo    🔄 Changed Files Review:      code-review-changed.bat
echo    📋 Full Code Review:          code-review-all.bat
echo    🔒 Security-Focused Review:   code-review-security.bat
echo    ⚡ Performance Review:        code-review-performance.bat
echo.
echo 📁 Important File Locations:
echo    • Scripts:           scripts\
echo    • Output Results:    code_review\review-output\
echo    • Templates:         code_review\review-output\templates\
echo    • Configuration:     .code-review-config
echo.
echo 📖 For detailed usage instructions, refer to README-code-review.md file.
echo.
echo 💡 The system supports execution from any directory within the project:
echo    - Project root (/Qcheck/)
echo    - Backend directory (/Qcheck/back/)
echo    - Frontend directory (/Qcheck/front/)
echo    - Code review directory (/Qcheck/code_review/)
echo.

pause