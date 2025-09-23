# =============================================================================
# 코드 추출 스크립트 (PowerShell) - Claude 코드리뷰용
# =============================================================================
# 사용법:
#   .\scripts\extract-code.ps1 -All                    # 전체 코드 추출
#   .\scripts\extract-code.ps1 -Changed                # 변경분만 추출
#   .\scripts\extract-code.ps1 -Since HEAD~3           # 특정 커밋 이후 변경분
#   .\scripts\extract-code.ps1 -Files "*.java"         # 특정 패턴 파일만
# =============================================================================

param(
    [switch]$All,
    [switch]$Changed,
    [string]$Since,
    [string]$Files,
    [switch]$Help
)

# UTF-8 인코딩 설정 (강화)
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['Out-File:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Add-Content:Encoding'] = 'UTF8'
$PSDefaultParameterValues['Set-Content:Encoding'] = 'UTF8'

# UTF-8 인코딩 객체 생성 (BOM 없음)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
# 색상 정의 (PowerShell 5.0+)
function Write-ColorText {
    param(
        [string]$Text,
        [string]$Color = "White"
    )
    Write-Host $Text -ForegroundColor $Color
}

# 설정 로드
$ConfigFile = ".code-review-config"
$Config = @{}

if (Test-Path $ConfigFile) {
    Write-ColorText "[CONFIG] 설정 파일 로드: $ConfigFile" "Yellow"
    Get-Content $ConfigFile | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') {
            $Config[$matches[1].Trim()] = $matches[2].Trim().Trim('"')
        }
    }
} else {
    Write-ColorText "[WARNING] 설정 파일이 없습니다. 기본값을 사용합니다." "Yellow"
    # 기본 설정값
    $Config = @{
        "TARGET_DIRS" = "../back/src ../front/src"
        "EXCLUDE_PATTERNS" = "node_modules target .git *.min.js *.map build dist coverage .nyc_output logs tmp temp"
        "INCLUDE_EXTENSIONS" = "java ts js html scss css xml properties json yaml yml"
        "MAX_FILE_SIZE" = "500"
        "OUTPUT_DIR" = "review-output"
    }
}

# 설정값 파싱
$TargetDirs = $Config["TARGET_DIRS"] -split '\s+'
$ExcludePatterns = $Config["EXCLUDE_PATTERNS"] -split '\s+'
$IncludeExtensions = $Config["INCLUDE_EXTENSIONS"] -split '\s+'
$MaxFileSize = [int]$Config["MAX_FILE_SIZE"]
$OutputDir = $Config["OUTPUT_DIR"]

# 출력 디렉토리 생성
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# 함수: 도움말 출력
function Show-Help {
    Write-Host "코드리뷰용 코드 추출 스크립트 (PowerShell)"
    Write-Host ""
    Write-Host "사용법:"
    Write-Host "  .\scripts\extract-code.ps1 -All                    전체 코드 추출"
    Write-Host "  .\scripts\extract-code.ps1 -Changed                최근 변경분 추출 (HEAD~1과 비교)"
    Write-Host "  .\scripts\extract-code.ps1 -Since <commit>         특정 커밋 이후 변경분 추출"
    Write-Host "  .\scripts\extract-code.ps1 -Files <pattern>        특정 파일 패턴만 추출"
    Write-Host "  .\scripts\extract-code.ps1 -Help                   이 도움말 출력"
    Write-Host ""
    Write-Host "예시:"
    Write-Host "  .\scripts\extract-code.ps1 -Changed"
    Write-Host "  .\scripts\extract-code.ps1 -Since HEAD~3"
    Write-Host "  .\scripts\extract-code.ps1 -Files `"*.java`""
    Write-Host "  .\scripts\extract-code.ps1 -All"
}

# 함수: 파일 크기 체크 (KB 단위)
function Test-FileSize {
    param([string]$FilePath)

    if (Test-Path $FilePath) {
        $sizeKB = (Get-Item $FilePath).Length / 1KB
        if ($sizeKB -gt $MaxFileSize) {
            Write-ColorText "[WARNING] 큰 파일 건너뜀: $FilePath ($([math]::Round($sizeKB))KB > $MaxFileSize KB)" "Yellow"
            return $false
        }
    }
    return $true
}

# 함수: 파일 확장자 체크
function Test-TargetFile {
    param([string]$FilePath)

    $ext = [System.IO.Path]::GetExtension($FilePath).TrimStart('.')
    return $ext -in $IncludeExtensions
}

# 함수: 제외 패턴 체크
function Test-ExcludedFile {
    param([string]$FilePath)

    foreach ($pattern in $ExcludePatterns) {
        if ($FilePath -like "*$pattern*") {
            return $true
        }
    }
    return $false
}

# 함수: 전체 코드 추출
function Extract-AllCode {
    Write-ColorText "[INFO] 전체 코드 추출 시작" "Cyan"

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    # 파일 초기화
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)
    [System.IO.File]::WriteAllText($fileListFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($dir in $TargetDirs) {
        if (Test-Path $dir) {
            Write-ColorText "[SCAN] $dir 디렉토리 스캔 중..." "Cyan"

            Get-ChildItem -Path $dir -Recurse -File | ForEach-Object {
                $file = $_.FullName
                $relativePath = $_.FullName.Substring((Get-Location).Path.Length + 1)

                # 제외 패턴 체크
                if (Test-ExcludedFile $relativePath) {
                    return
                }

                # 대상 파일인지 체크
                if (!(Test-TargetFile $_.Name)) {
                    return
                }

                # 파일 크기 체크
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
            Write-ColorText "`[WARNING`] 디렉토리가 없습니다: $dir" "Yellow"
        }
    }

    Write-ColorText "`[SUCCESS`] 전체 코드 추출 완료: $fileCount 개 파일, $lineCount 줄" "Green"
    return $fileCount
}

# 함수: 변경분 추출
function Extract-ChangedCode {
    param([string]$SinceCommit = "HEAD~1")

    Write-ColorText "`[INFO`] 변경분 추출 시작 (기준: $SinceCommit)" "Cyan"

    # Git 저장소 확인
    try {
        git rev-parse --git-dir | Out-Null
    } catch {
        Write-ColorText "[ERROR] Git 저장소가 아닙니다." "Red"
        exit 1
    }

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"
    $diffFile = Join-Path $OutputDir "diff-summary.txt"

    # Git diff 정보 생성
    Write-ColorText "[INFO] Git diff 정보 생성 중..." "Cyan"
    $diffStat = git diff --stat $SinceCommit | Out-String
    [System.IO.File]::WriteAllText($diffFile, $diffStat, $utf8NoBom)
    $diffContent = git diff $SinceCommit | Out-String
    [System.IO.File]::AppendAllText($diffFile, $diffContent, $utf8NoBom)

    # 변경된 파일 목록 가져오기
    $changedFiles = git diff --name-only $SinceCommit
    $fileListContent = $changedFiles | Out-String
    [System.IO.File]::WriteAllText($fileListFile, $fileListContent, $utf8NoBom)

    if (!$changedFiles) {
        Write-ColorText "`[WARNING`] 변경된 파일이 없습니다." "Yellow"
        return 0
    }

    # 파일 초기화
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($file in $changedFiles) {
        # 파일이 존재하는지 확인 (삭제된 파일일 수 있음)
        if (!(Test-Path $file)) {
            Write-ColorText " `[DELETED`] 삭제된 파일: $file" "Gray"
            continue
        }

        # 제외 패턴 체크
        if (Test-ExcludedFile $file) {
            continue
        }

        # 대상 파일인지 체크
        if (!(Test-TargetFile (Split-Path -Leaf $file))) {
            continue
        }

        # 파일 크기 체크
        if (!(Test-FileSize $file)) {
            continue
        }

        [System.IO.File]::AppendAllText($codeFile, "=== $file ===$([Environment]::NewLine)", $utf8NoBom)
        $fileContent = Get-Content -Path $file -Encoding UTF8 -Raw
        [System.IO.File]::AppendAllText($codeFile, $fileContent, $utf8NoBom)
        [System.IO.File]::AppendAllText($codeFile, "$([Environment]::NewLine)$([Environment]::NewLine)", $utf8NoBom)

        $lines = (Get-Content -Path $file).Count
        $fileCount++
        $lineCount += $lines

        Write-ColorText "  `[OK`] $file - $lines lines" "Green"
    }

    Write-ColorText "`[SUCCESS`] 변경분 추출 완료: $fileCount 개 파일, $lineCount 줄" "Green"
    return $fileCount
}

# 함수: 특정 파일 패턴 추출
function Extract-PatternFiles {
    param([string]$Pattern)

    Write-ColorText "[SEARCH] 패턴별 파일 추출: $Pattern" "Cyan"

    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    # 파일 초기화
    [System.IO.File]::WriteAllText($codeFile, "", $utf8NoBom)
    [System.IO.File]::WriteAllText($fileListFile, "", $utf8NoBom)

    $fileCount = 0
    $lineCount = 0

    foreach ($dir in $TargetDirs) {
        if (Test-Path $dir) {
            Get-ChildItem -Path $dir -Recurse -File -Filter $Pattern | ForEach-Object {
                $file = $_.FullName
                $relativePath = $_.FullName.Substring((Get-Location).Path.Length + 1)

                # 제외 패턴 체크
                if (Test-ExcludedFile $relativePath) {
                    return
                }

                # 파일 크기 체크
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

                Write-ColorText " `[OK`] $relativePath - $lines lines" "Green"
            }
        }
    }

    Write-ColorText "`[SUCCESS`] 패턴 파일 추출 완료: $fileCount 개 파일, $lineCount 줄" "Green"
    return $fileCount
}

# 메인 실행 로직
Write-ColorText "[START] Claude 코드리뷰용 코드 추출 스크립트 (PowerShell)" "Green"
Write-Host "================================================"

# 파라미터 검증
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
    Write-ColorText "X 옵션을 지정해주세요." "Red"
    Show-Help
    exit 1
}

# 프롬프트 생성
if ($fileCount -gt 0) {
    Write-ColorText "`[INFO`] 프롬프트 생성 중..." "Cyan"

    # PowerShell 프롬프트 생성 스크립트 호출
    if (Test-Path "scripts\generate-prompt.ps1") {
        & "scripts\generate-prompt.ps1" -Mode $mode
    } else {
        Write-ColorText "`[WARNING`] generate-prompt.ps1을 찾을 수 없습니다." "Yellow"
    }

    Write-Host ""
    Write-ColorText "`[SUCCESS`] 코드 추출 완료!" "Green"
    Write-Host ""
    Write-ColorText "[OUTPUT] 출력 파일:" "Cyan"
    Write-Host "  - $OutputDir\code-to-review.txt - $fileCount files"
    Write-Host "  - $OutputDir\review-prompt.txt (완성된 프롬프트)"
    Write-Host "  - $OutputDir\file-list.txt (파일 목록)"
    if ($mode -eq "changed") {
        Write-Host "  - $OutputDir\diff-summary.txt (Git diff 요약)"
    }
    Write-Host ""
    Write-ColorText "[NEXT] 다음 단계:" "Yellow"
    Write-Host "  1. $OutputDir\review-prompt.txt 내용을 Claude에게 복사"
    Write-Host "  2. 리뷰 결과를 $OutputDir\claude-response.txt에 저장"
    Write-Host "  3. 개선사항 적용 후 재검토"

} else {
    Write-ColorText "`[WARNING`] No files extracted" "Yellow"
}