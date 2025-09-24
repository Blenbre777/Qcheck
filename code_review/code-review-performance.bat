@echo off
chcp 65001 >nul
echo.
echo ===============================================
echo    Qcheck Code Review System - Performance Optimization Review
echo ===============================================
echo.

echo [PERFORMANCE] Analyzing code from performance optimization perspective...
echo.

echo Step 1: Extracting code...
REM PowerShell script execution (based on changes)
powershell -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '.\code_review\scripts\extract-code.ps1' -Changed"

if %ERRORLEVEL% == 0 (
    echo.
    echo Step 2: Generating performance-focused prompt...

    REM Copy performance template
    if exist "code_review\review-output\templates\performance-review-prompt.txt" (
        copy "code_review\review-output\templates\performance-review-prompt.txt" "code_review\review-output\performance-prompt.txt" >nul

        echo.
        echo [SUCCESS] Performance optimization code review prompt generated!
        echo.
        echo 📁 Generated Files:
        echo    - code_review\review-output\performance-prompt.txt ^(Performance-focused prompt template^)
        echo    - code_review\review-output\code-to-review.txt ^(Code to analyze^)
        echo    - code_review\review-output\file-list.txt ^(Changed file list^)
        echo.
        echo [PERFORMANCE] Performance Review Items:
        echo    ✓ N+1 query problem analysis
        echo    ✓ Algorithm complexity optimization
        echo    ✓ Memory usage improvement
        echo    ✓ Database query optimization
        echo    ✓ Caching strategy review
        echo    ✓ I/O operation optimization
        echo    ✓ Frontend rendering performance
        echo.
        echo 📊 Performance Metrics:
        echo    • Response Time
        echo    • Throughput
        echo    • Memory Usage
        echo    • CPU Utilization
        echo    • Database Query Time
        echo.
        echo 📋 Next Steps:
        echo    1. Edit code_review\review-output\performance-prompt.txt file
        echo    2. Replace {CODE_INSERTION_POINT} with actual code
        echo    3. Request performance optimization review from Claude
        echo.

        REM Automatically open performance prompt file
        set /p openfile="[INFO] Would you like to edit the performance prompt file? (y/n): "
        if /i "%openfile%"=="y" (
            echo [INFO] Opening performance prompt file in default editor...
            start "" "code_review\review-output\performance-prompt.txt"
            echo.
            echo 💡 Editing Guide:
            echo    - Find the {CODE_INSERTION_POINT} text at the bottom of the file
            echo    - Replace that section with contents from code_review\review-output\code-to-review.txt
            echo    - Submit the completed prompt to Claude
            echo    - Request priority and expected impact of performance issues
        )
    ) else (
        echo [WARNING] Performance template file not found.
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