@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck Code Review System - Full Code Review
echo ===============================================
echo.

echo [INFO] Extracting entire codebase to generate Claude review prompt...
echo [WARNING] This may take some time if there are many files.
echo.

REM PowerShell script execution
powershell -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '.\code_review\scripts\extract-code.ps1' -All"

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] Full code extraction and prompt generation completed!
    echo.
    echo 📁 Result Files:
    echo    - code_review\review-output\review-prompt.txt ^(Prompt to copy to Claude^)
    echo    - code_review\review-output\code-to-review.txt ^(Extracted full code^)
    echo    - code_review\review-output\file-list.txt ^(Included file list^)
    echo.
    echo 📋 Next Steps:
    echo    1. Open code_review\review-output\review-prompt.txt file and copy its content
    echo    2. Paste into Claude to request full code review
    echo    3. Save review results to code_review\review-output\claude-response.txt
    echo.
    echo 💡 Tip: If the full code is too large, consider splitting it for partial reviews.
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
    echo ❌ An error occurred during code extraction.
    echo 💡 Troubleshooting:
    echo    1. Verify target directories ^(back/src, front/src^) exist
    echo    2. Check file size limit settings ^(.code-review-config^)
    echo    3. Verify PowerShell execution policy
    echo.
)

echo.
pause