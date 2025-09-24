@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck Code Review System - Changed Files Review
echo ===============================================
echo.

echo [INFO] Extracting changed files to generate Claude review prompt...
echo [INFO] This will extract files modified since the last commit (HEAD~1).
echo.

REM PowerShell script execution
powershell -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '.\code_review\scripts\extract-code.ps1' -Changed"

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] Changed files extraction and prompt generation completed!
    echo.
    echo 📁 Result Files:
    echo    - code_review\review-output\review-prompt.txt ^(Prompt to copy to Claude^)
    echo    - code_review\review-output\code-to-review.txt ^(Changed files code^)
    echo    - code_review\review-output\file-list.txt ^(Changed file list^)
    echo    - code_review\review-output\diff-summary.txt ^(Git diff summary^)
    echo.
    echo 📋 Next Steps:
    echo    1. Open code_review\review-output\review-prompt.txt file and copy its content
    echo    2. Paste into Claude to request changed files review
    echo    3. Save review results to code_review\review-output\claude-response.txt
    echo.
    echo 💡 Tip: This focuses on recent changes for more targeted review.
    echo.

    REM Automatically open result file (optional)
    set /p openfile="[INFO] Would you like to open the result file automatically? (y/n): "
    if /i "%openfile%"=="y" (
        if exist "code_review\review-output\review-prompt.txt" (
            echo [INFO] Opening prompt file in default editor...
            start "" "code_review\review-output\review-prompt.txt"
        )
    )
) else (
    echo.
    echo ❌ An error occurred during changed files extraction.
    echo 💡 Troubleshooting:
    echo    1. Ensure this is a Git repository
    echo    2. Verify there are committed changes to compare
    echo    3. Check if target directories exist
    echo    4. Verify PowerShell execution policy
    echo.
)

echo.
pause