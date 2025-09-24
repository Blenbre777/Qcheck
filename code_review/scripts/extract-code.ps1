# =============================================================================
# Code Extraction Script (PowerShell) - For Claude Code Review
# =============================================================================
# Usage:
#   .\scripts\extract-code.ps1 -All                    # Extract all code
#   .\scripts\extract-code.ps1 -Changed                # Extract only changes
#   .\scripts\extract-code.ps1 -Since HEAD~3           # Extract changes since specific commit
#   .\scripts\extract-code.ps1 -Files "*.java"         # Extract specific file patterns
# =============================================================================

param(
    [switch]$All,
    [switch]$Changed,
    [string]$Since,
    [string]$Files,
    [switch]$Help
)

# UTF-8 encoding setup (enhanced)
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['Out-File:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Add-Content:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Set-Content:Encoding'] = 'UTF8'

# UTF-8 encoding object creation (without BOM)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# Function: Find project root directory
function Find-ProjectRoot {
    $currentDir = Get-Location
    while ($currentDir.Path -ne $currentDir.Root.Path) {
        if ((Test-Path (Join-Path $currentDir.Path ".git")) -or
            (Test-Path (Join-Path $currentDir.Path "code_review")) -or
            ((Test-Path (Join-Path $currentDir.Path "back")) -and (Test-Path (Join-Path $currentDir.Path "front")))) {
            return $currentDir.Path
        }
        $currentDir = $currentDir.Parent
    }
    throw "Project root directory not found. Please run this script from within the Qcheck project."
}

# Set project root and working directory
try {
    $ProjectRoot = Find-ProjectRoot
    Set-Location $ProjectRoot
    Write-Host "[INFO] Project root detected: $ProjectRoot" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] $_" -ForegroundColor Red
    exit 1
}

# Color output function (PowerShell 5.0+)
function Write-ColorText {
    param(
        [string]$Text,
        [string]$Color = "White"
    )
    Write-Host $Text -ForegroundColor $Color
}

# Load configuration
$ConfigFile = "code_review\.code-review-config"
$Config = @{}

if (Test-Path $ConfigFile) {
    Write-ColorText "[CONFIG] Loading configuration file: $ConfigFile" "Yellow"
    Get-Content $ConfigFile | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') {
            $Config[$matches[1].Trim()] = $matches[2].Trim().Trim('"')
        }
    }
} else {
    Write-ColorText "[WARNING] Configuration file not found. Using default values." "Yellow"
    # Default configuration values
    $Config = @{
        "TARGET_DIRS" = "back/src front/src"
        "EXCLUDE_PATTERNS" = "node_modules target .git *.min.js *.map build dist coverage .nyc_output logs tmp temp"
        "INCLUDE_EXTENSIONS" = "java ts js html scss css xml properties json yaml yml"
        "MAX_FILE_SIZE" = "500"
        "OUTPUT_DIR" = "code_review/review-output"
    }
}

# Parse configuration values
$TargetDirs = $Config["TARGET_DIRS"] -split '\s+' | ForEach-Object { Join-Path $ProjectRoot $_ }
$ExcludePatterns = $Config["EXCLUDE_PATTERNS"] -split '\s+'
$IncludeExtensions = $Config["INCLUDE_EXTENSIONS"] -split '\s+'
$MaxFileSize = [int]$Config["MAX_FILE_SIZE"]
$OutputDir = Join-Path $ProjectRoot $Config["OUTPUT_DIR"]

# Create output directory
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Function: Show help
function Show-Help {
    Write-Host "Code Extraction Script for Code Review (PowerShell)"
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\scripts\extract-code.ps1 -All                    Extract all code"
    Write-Host "  .\scripts\extract-code.ps1 -Changed                Extract recent changes (compare with HEAD~1)"
    Write-Host "  .\scripts\extract-code.ps1 -Since <commit>         Extract changes since specific commit"
    Write-Host "  .\scripts\extract-code.ps1 -Files <pattern>        Extract specific file patterns only"
    Write-Host "  .\scripts\extract-code.ps1 -Help                   Show this help"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\scripts\extract-code.ps1 -Changed"
    Write-Host "  .\scripts\extract-code.ps1 -Since HEAD~3"
    Write-Host "  .\scripts\extract-code.ps1 -Files `"*.java`""
    Write-Host "  .\scripts\extract-code.ps1 -All"
}

# Function: Check file size (in KB)
function Test-FileSize {
    param([string]$FilePath)

    if (Test-Path $FilePath) {
        $sizeKB = (Get-Item $FilePath).Length / 1KB
        if ($sizeKB -gt $MaxFileSize) {
            Write-ColorText "[WARNING] Skipping large file: $FilePath ($([math]::Round($sizeKB))KB > $MaxFileSize KB)" "Yellow"
            return $false
        }
    }
    return $true
}

# Function: Check file extension
function Test-TargetFile {
    param([string]$FilePath)

    $ext = [System.IO.Path]::GetExtension($FilePath).TrimStart('.')
    return $ext -in $IncludeExtensions
}

# Function: Check exclude patterns
function Test-ExcludedFile {
    param([string]$FilePath)

    foreach ($pattern in $ExcludePatterns) {
        if ($FilePath -like "*$pattern*") {
            return $true
        }
    }
    return $false
}

# Function: Extract all code
function Extract-AllCode {
    Write-ColorText "[INFO] Starting full code extraction" "Cyan"

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    # Initialize files
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)
    [System.IO.File]::WriteAllText($fileListFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($dir in $TargetDirs) {
        if (Test-Path $dir) {
            Write-ColorText "[SCAN] Scanning directory: $dir" "Cyan"

            Get-ChildItem -Path $dir -Recurse -File | ForEach-Object {
                $file = $_.FullName
                $relativePath = $_.FullName.Substring($ProjectRoot.Length + 1)

                # Check exclude patterns
                if (Test-ExcludedFile $relativePath) {
                    return
                }

                # Check if target file
                if (!(Test-TargetFile $_.Name)) {
                    return
                }

                # Check file size
                if (!(Test-FileSize $file)) {
                    return
                }

                [System.IO.File]::AppendAllText($fileListFile, "$relativePath$([Environment]::NewLine)", $utf8NoBom)
                [System.IO.File]::AppendAllText($codeFile, "=== $relativePath ===$([Environment]::NewLine)", $utf8NoBom)
                $fileContent = Get-Content -Path $file -Encoding UTF8 -Raw
                [System.IO.File]::AppendAllText($codeFile, $fileContent, $utf8NoBom)
                [System.IO.File]::AppendAllText($codeFile, "$([Environment]::NewLine)$([Environment]::NewLine)", $utf8NoBom)

                $lines = (Get-Content -Path $file).Count
                $fileCount++
                $lineCount += $lines

                Write-ColorText "  [OK] $relativePath ($lines lines)" "Green"
            }
        } else {
            Write-ColorText "[WARNING] Directory not found: $dir" "Yellow"
        }
    }

    Write-ColorText "[SUCCESS] Full code extraction completed: $fileCount files, $lineCount lines" "Green"
    return $fileCount
}

# Function: Extract changed code
function Extract-ChangedCode {
    param([string]$SinceCommit = "HEAD~1")

    Write-ColorText "[INFO] Starting changed code extraction (since: $SinceCommit)" "Cyan"

    # Check Git repository
    try {
        git rev-parse --git-dir | Out-Null
    } catch {
        Write-ColorText "[ERROR] Not a Git repository." "Red"
        exit 1
    }

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"
    $diffFile = Join-Path $OutputDir "diff-summary.txt"

    # Generate Git diff information
    Write-ColorText "[INFO] Generating Git diff information..." "Cyan"
    $diffStat = git diff --stat $SinceCommit | Out-String
    [System.IO.File]::WriteAllText($diffFile, $diffStat, $utf8NoBom)
    $diffContent = git diff $SinceCommit | Out-String
    [System.IO.File]::AppendAllText($diffFile, $diffContent, $utf8NoBom)

    # Get list of changed files
    $changedFiles = git diff --name-only $SinceCommit
    $fileListContent = $changedFiles | Out-String
    [System.IO.File]::WriteAllText($fileListFile, $fileListContent, $utf8NoBom)

    if (!$changedFiles) {
        Write-ColorText "[WARNING] No changed files found." "Yellow"
        return 0
    }

    # Initialize files
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($file in $changedFiles) {
        # Convert to absolute path from project root
        $absoluteFile = Join-Path $ProjectRoot $file

        # Check if file exists (might be deleted)
        if (!(Test-Path $absoluteFile)) {
            Write-ColorText " [DELETED] Deleted file: $file" "Gray"
            continue
        }

        # Check exclude patterns
        if (Test-ExcludedFile $file) {
            continue
        }

        # Check if target file
        if (!(Test-TargetFile (Split-Path -Leaf $file))) {
            continue
        }

        # Check file size
        if (!(Test-FileSize $absoluteFile)) {
            continue
        }

        [System.IO.File]::AppendAllText($codeFile, "=== $file ===$([Environment]::NewLine)", $utf8NoBom)
        $fileContent = Get-Content -Path $absoluteFile -Encoding UTF8 -Raw
        [System.IO.File]::AppendAllText($codeFile, $fileContent, $utf8NoBom)
        [System.IO.File]::AppendAllText($codeFile, "$([Environment]::NewLine)$([Environment]::NewLine)", $utf8NoBom)

        $lines = (Get-Content -Path $absoluteFile).Count
        $fileCount++
        $lineCount += $lines

        Write-ColorText "  [OK] $file - $lines lines" "Green"
    }

    Write-ColorText "[SUCCESS] Changed code extraction completed: $fileCount files, $lineCount lines" "Green"
    return $fileCount
}

# Function: Extract files by pattern
function Extract-PatternFiles {
    param([string]$Pattern)

    Write-ColorText "[SEARCH] Extracting files by pattern: $Pattern" "Cyan"

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    # Initialize files
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)
    [System.IO.File]::WriteAllText($fileListFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($dir in $TargetDirs) {
        if (Test-Path $dir) {
            Get-ChildItem -Path $dir -Recurse -File -Filter $Pattern | ForEach-Object {
                $file = $_.FullName
                $relativePath = $_.FullName.Substring($ProjectRoot.Length + 1)

                # Check exclude patterns
                if (Test-ExcludedFile $relativePath) {
                    return
                }

                # Check file size
                if (!(Test-FileSize $file)) {
                    return
                }

                [System.IO.File]::AppendAllText($fileListFile, "$relativePath$([Environment]::NewLine)", $utf8NoBom)
                [System.IO.File]::AppendAllText($codeFile, "=== $relativePath ===$([Environment]::NewLine)", $utf8NoBom)
                $fileContent = Get-Content -Path $file -Encoding UTF8 -Raw
                [System.IO.File]::AppendAllText($codeFile, $fileContent, $utf8NoBom)
                [System.IO.File]::AppendAllText($codeFile, "$([Environment]::NewLine)$([Environment]::NewLine)", $utf8NoBom)

                $lines = (Get-Content -Path $file).Count
                $fileCount++
                $lineCount += $lines

                Write-ColorText " [OK] $relativePath - $lines lines" "Green"
            }
        }
    }

    Write-ColorText "[SUCCESS] Pattern file extraction completed: $fileCount files, $lineCount lines" "Green"
    return $fileCount
}

# Main execution logic
Write-ColorText "[START] Claude Code Review Code Extraction Script (PowerShell)" "Green"
Write-Host "================================================"

# Parameter validation
if ($Help) {
    Show-Help
    exit 0
}

$mode = ""
$fileCount = 0

if ($All) {
    $mode = "all"
    $fileCount = Extract-AllCode
} elseif ($Changed) {
    $mode = "changed"
    $fileCount = Extract-ChangedCode
} elseif ($Since) {
    $mode = "changed"
    $fileCount = Extract-ChangedCode $Since
} elseif ($Files) {
    $mode = "pattern"
    $fileCount = Extract-PatternFiles $Files
} else {
    Write-ColorText "[ERROR] Please specify an option." "Red"
    Show-Help
    exit 1
}

# Generate prompt
if ($fileCount -gt 0) {
    Write-ColorText "[INFO] Generating prompt..." "Cyan"

    # Call PowerShell prompt generation script
    $generateScriptPath = Join-Path $ProjectRoot "code_review\scripts\generate-prompt.ps1"
    if (Test-Path $generateScriptPath) {
        & $generateScriptPath -Mode $mode
    } else {
        Write-ColorText "[WARNING] generate-prompt.ps1 not found." "Yellow"
    }

    Write-Host ""
    Write-ColorText "[SUCCESS] Code extraction completed!" "Green"
    Write-Host ""
    Write-ColorText "[OUTPUT] Output files:" "Cyan"
    Write-Host "  - $($Config["OUTPUT_DIR"])\code-to-review.txt - $fileCount files"
    Write-Host "  - $($Config["OUTPUT_DIR"])\review-prompt.txt (completed prompt)"
    Write-Host "  - $($Config["OUTPUT_DIR"])\file-list.txt (file list)"
    if ($mode -eq "changed") {
        Write-Host "  - $($Config["OUTPUT_DIR"])\diff-summary.txt (Git diff summary)"
    }
    Write-Host ""
    Write-ColorText "[NEXT] Next steps:" "Yellow"
    Write-Host "  1. Copy contents of $($Config["OUTPUT_DIR"])\review-prompt.txt to Claude"
    Write-Host "  2. Save review results to $($Config["OUTPUT_DIR"])\claude-response.txt"
    Write-Host "  3. Apply improvements and re-review"

} else {
    Write-ColorText "[WARNING] No files extracted" "Yellow"
}