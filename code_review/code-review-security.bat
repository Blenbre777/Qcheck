@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck Code Review System - Security-Focused Review
echo ===============================================
echo.

echo [SECURITY] Analyzing code with focus on security vulnerabilities...
echo.

echo Step 1: Extracting code...
REM PowerShell script execution (based on changes)
powershell -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '.\code_review\scripts\extract-code.ps1' -Changed"

if %ERRORLEVEL% == 0 (
    echo.
    echo Step 2: Generating security-focused prompt...

    REM Copy security template and insert code
    if exist "code_review\review-output\templates\security-review-prompt.txt" (
        copy "code_review\review-output\templates\security-review-prompt.txt" "code_review\review-output\security-prompt.txt" >nul

        echo.
        echo [SUCCESS] Security-focused code review prompt generated!
        echo.
        echo 📁 Generated Files:
        echo    - code_review\review-output\security-prompt.txt ^(Security-focused prompt template^)
        echo    - code_review\review-output\code-to-review.txt ^(Code to analyze^)
        echo    - code_review\review-output\file-list.txt ^(Changed file list^)
        echo.
        echo [SECURITY] Security Review Items:
        echo    ✓ SQL Injection vulnerabilities
        echo    ✓ XSS ^(Cross-Site Scripting^) attacks
        echo    ✓ Authentication/Authorization logic
        echo    ✓ Sensitive information exposure prevention
        echo    ✓ Input validation and sanitization
        echo    ✓ File upload security
        echo    ✓ Session management security
        echo.
        echo 📋 Next Steps:
        echo    1. Edit code_review\review-output\security-prompt.txt file
        echo    2. Replace {CODE_INSERTION_POINT} with actual code
        echo    3. Request security review from Claude
        echo.

        REM Automatically open security prompt file
        set /p openfile="[INFO] Would you like to edit the security prompt file? (y/n): "
        if /i "%openfile%"=="y" (
            echo [INFO] Opening security prompt file in default editor...
            start "" "code_review\review-output\security-prompt.txt"
            echo.
            echo 💡 Editing Guide:
            echo    - Find the {CODE_INSERTION_POINT} text at the bottom of the file
            echo    - Replace that section with contents from code_review\review-output\code-to-review.txt
            echo    - Submit the completed prompt to Claude
        )
    ) else (
        echo [WARNING] Security template file not found.
        echo 💡 Please run scripts\create-prompt-templates.ps1 first.
    )
) else (
    echo.
    echo ❌ An error occurred during code extraction.
    echo 💡 Troubleshooting:
    echo    1. Ensure this is a Git repository
    echo    2. Verify there are changed files
    echo    3. Check PowerShell execution policy
    echo.
    echo 🔧 Manual execution method:
    echo    powershell -ExecutionPolicy Bypass -Command "& '.\code_review\scripts\extract-code.ps1' -Changed"
)

echo.
pause