# =============================================================================
# Prompt Generation Script (PowerShell) - For Claude Code Review
# =============================================================================
# Generates code review prompts based on extracted code for Claude AI.
# =============================================================================

param(
    [string]$Mode = "basic"
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
} catch {
    Write-Host "[ERROR] $_" -ForegroundColor Red
    exit 1
}

# Color output function
function Write-ColorText {
    param(
        [string]$Text,
        [string]$Color = "White"
    )
    Write-Host $Text -ForegroundColor $Color
}

# Load configuration
$ConfigFile = "code_review\.code-review-config"
$OutputDir = "review-output"

if (Test-Path $ConfigFile) {
    Get-Content $ConfigFile | ForEach-Object {
        if ($_ -match '^OUTPUT_DIR=(.*)$') {
            $OutputDir = $matches[1].Trim().Trim('"')
        }
    }
}

# Ensure we have absolute path for output directory
$OutputDir = Join-Path $ProjectRoot $OutputDir

# Function: Generate basic review prompt
function New-BasicPrompt {
    $promptFile = Join-Path $OutputDir "review-prompt.txt"
    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"

    if (!(Test-Path $codeFile)) {
        Write-ColorText "[ERROR] Code file not found: $codeFile" "Red"
        return
    }

    # File statistics
    $fileCount = (Get-Content $fileListFile | Measure-Object -Line).Lines
    $lineCount = (Get-Content $codeFile | Measure-Object -Line).Lines

    # Basic prompt template
    $promptContent = "# Project Information$([Environment]::NewLine)"
    $promptContent += "- **Project Name**: Qcheck (Question Check System)$([Environment]::NewLine)"
    $promptContent += "- **Backend**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate$([Environment]::NewLine)"
    $promptContent += "- **Frontend**: Angular 20, TypeScript, SCSS$([Environment]::NewLine)"
    $promptContent += "- **Architecture**: RESTful API, SPA (Single Page Application)$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "# Code Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "Please perform a professional and detailed code review for the following code.$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "## [GOALS] Review Perspectives$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 1. **Code Quality**$([Environment]::NewLine)"
    $promptContent += "- Readability: Clarity of variable names, method names$([Environment]::NewLine)"
    $promptContent += "- Maintainability: Code structure, modularization$([Environment]::NewLine)"
    $promptContent += "- Reusability: Elimination of duplicate code, extraction of common functionality$([Environment]::NewLine)"
    $promptContent += "- Consistency: Adherence to coding conventions$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 2. **Performance Optimization**$([Environment]::NewLine)"
    $promptContent += "- Inefficient algorithms or logic$([Environment]::NewLine)"
    $promptContent += "- Memory usage optimization$([Environment]::NewLine)"
    $promptContent += "- Database query optimization (N+1 problem, index utilization)$([Environment]::NewLine)"
    $promptContent += "- Elimination of unnecessary operations$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 3. **Security**$([Environment]::NewLine)"
    $promptContent += "- SQL injection, XSS and other web vulnerabilities$([Environment]::NewLine)"
    $promptContent += "- Input validation and sanitization$([Environment]::NewLine)"
    $promptContent += "- Authorization and authentication handling$([Environment]::NewLine)"
    $promptContent += "- Prevention of sensitive information exposure$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 4. **Architecture & Design**$([Environment]::NewLine)"
    $promptContent += "- Application of Spring Boot best practices$([Environment]::NewLine)"
    $promptContent += "- Adherence to Angular architecture patterns$([Environment]::NewLine)"
    $promptContent += "- Utilization of dependency injection and IoC$([Environment]::NewLine)"
    $promptContent += "- Layer separation (Controller, Service, Repository)$([Environment]::NewLine)"
    $promptContent += "- Application of design patterns$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 5. **Error Handling & Reliability**$([Environment]::NewLine)"
    $promptContent += "- Exception handling strategy$([Environment]::NewLine)"
    $promptContent += "- Null checks and defensive programming$([Environment]::NewLine)"
    $promptContent += "- Boundary condition handling$([Environment]::NewLine)"
    $promptContent += "- Resource management (Connection, Stream etc.)$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 6. **Testing & Quality Assurance**$([Environment]::NewLine)"
    $promptContent += "- Unit test writability$([Environment]::NewLine)"
    $promptContent += "- Integration test considerations$([Environment]::NewLine)"
    $promptContent += "- Test coverage improvement methods$([Environment]::NewLine)"
    $promptContent += "- Mock object utilization$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "### 7. **Documentation**$([Environment]::NewLine)"
    $promptContent += "- JavaDoc/TSDoc writing$([Environment]::NewLine)"
    $promptContent += "- Complex logic explanations$([Environment]::NewLine)"
    $promptContent += "- API documentation necessity$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "## [STATISTICS] Code Statistics$([Environment]::NewLine)"
    $promptContent += "- **Total Files**: ${fileCount} files$([Environment]::NewLine)"
    $promptContent += "- **Total Lines**: ${lineCount} lines$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptContent += "## [FILES] File List$([Environment]::NewLine)"
    $promptContent += "```$([Environment]::NewLine)"

    # Add file list
    [System.IO.File]::WriteAllText($promptFile, $promptContent, $utf8NoBom)
    $fileListContent = Get-Content $fileListFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $fileListContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # Add code content
    [System.IO.File]::AppendAllText($promptFile, "## [CODE] Code Content$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $codeContent = Get-Content $codeFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $codeContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # Add request requirements
    $requestContent = "## [REQUEST] Requirements$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 1. **Issue Analysis & Classification**$([Environment]::NewLine)"
    $requestContent += "Please classify each identified issue as follows:$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "- **[CRITICAL]**: Security vulnerabilities, serious bug potential$([Environment]::NewLine)"
    $requestContent += "- **[HIGH]**: Performance issues, architectural problems$([Environment]::NewLine)"
    $requestContent += "- **[MEDIUM]**: Code quality, maintainability improvements$([Environment]::NewLine)"
    $requestContent += "- **[LOW]**: Coding conventions, style guide$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 2. **Specific Improvement Solutions**$([Environment]::NewLine)"
    $requestContent += "- Provide **specific solutions** rather than just pointing out problems$([Environment]::NewLine)"
    $requestContent += "- Include **Before/After code examples**$([Environment]::NewLine)"
    $requestContent += "- Explain **why such improvements are necessary**$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 3. **Framework-Specific Best Practices**$([Environment]::NewLine)"
    $requestContent += "- **Spring Boot**: Proper use of @Service, @Repository, @Transactional etc.$([Environment]::NewLine)"
    $requestContent += "- **JPA/Hibernate**: Entity design, query optimization, lazy loading$([Environment]::NewLine)"
    $requestContent += "- **Angular**: Component design, service patterns, RxJS utilization$([Environment]::NewLine)"
    $requestContent += "- **TypeScript**: Type safety, interface utilization$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 4. **Priority Recommendations**$([Environment]::NewLine)"
    $requestContent += "Please present improvements sorted by priority.$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 5. **Additional Considerations**$([Environment]::NewLine)"
    $requestContent += "- Suggestions for future extensibility$([Environment]::NewLine)"
    $requestContent += "- Performance monitoring points$([Environment]::NewLine)"
    $requestContent += "- Areas requiring additional testing$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "---$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "**[NOTE]**: Please provide a review with **educational value** rather than simple problem identification. Explaining **why each suggestion is important** and **what benefits it brings** would be extremely helpful.$([Environment]::NewLine)$([Environment]::NewLine)"
    $requestContent += "### 6. **Language Translation Request**$([Environment]::NewLine)"
    $requestContent += "**Please provide your entire code review response translated into Korean at the end.$([Environment]::NewLine)"

    [System.IO.File]::AppendAllText($promptFile, $requestContent, $utf8NoBom)

    Write-ColorText "[SUCCESS] Basic prompt generated successfully: $promptFile" "Green"
}

# Function: Generate changed code prompt
function New-ChangedPrompt {
    $promptFile = Join-Path $OutputDir "review-prompt.txt"
    $codeFile = Join-Path $OutputDir "code-to-review.txt"
    $fileListFile = Join-Path $OutputDir "file-list.txt"
    $diffFile = Join-Path $OutputDir "diff-summary.txt"

    # Check if Git diff file exists
    if (!(Test-Path $diffFile)) {
        Write-ColorText "[WARNING] Git diff file not found, generating basic prompt." "Yellow"
        New-BasicPrompt
        return
    }

    # File statistics
    $fileCount = (Get-Content $fileListFile | Measure-Object -Line).Lines
    $lineCount = (Get-Content $codeFile | Measure-Object -Line).Lines

    # Extract Git diff statistics
    $diffStats = Get-Content $diffFile -TotalCount 20 | Where-Object { $_ -match "files? changed|insertions?|deletions?" } | Select-Object -First 1

    # Changed code prompt template
    $promptTemplate = "# Changed Code Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptTemplate += "## Project Information$([Environment]::NewLine)"
    $promptTemplate += "- **Project Name**: Qcheck (Question Check System)$([Environment]::NewLine)"
    $promptTemplate += "- **Backend**: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate$([Environment]::NewLine)"
    $promptTemplate += "- **Frontend**: Angular 20, TypeScript, SCSS$([Environment]::NewLine)"
    $promptTemplate += "- **Architecture**: RESTful API, SPA (Single Page Application)$([Environment]::NewLine)$([Environment]::NewLine)"
    $promptTemplate += "## Change Summary$([Environment]::NewLine)"
    $promptTemplate += "- **Changed Files**: ${fileCount} files$([Environment]::NewLine)"
    $promptTemplate += "- **Total Lines**: ${lineCount} lines$([Environment]::NewLine)"

    if ($diffStats) {
        $promptTemplate += "- **Git Statistics**: $diffStats$([Environment]::NewLine)"
    }

    $promptTemplate += "$([Environment]::NewLine)## [STATISTICS] Git Diff Summary$([Environment]::NewLine)```diff$([Environment]::NewLine)"

    # Create prompt file
    [System.IO.File]::WriteAllText($promptFile, $promptTemplate, $utf8NoBom)
    $diffContent = Get-Content $diffFile -TotalCount 50 -Encoding UTF8 | Out-String
    [System.IO.File]::AppendAllText($promptFile, $diffContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # Add file list
    [System.IO.File]::AppendAllText($promptFile, "## [FILES] Changed File List$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $fileListContent = Get-Content $fileListFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $fileListContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # Add changed file code
    [System.IO.File]::AppendAllText($promptFile, "## [CODE] Complete Code for Changed Files$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    $codeContent = Get-Content $codeFile -Encoding UTF8 -Raw
    [System.IO.File]::AppendAllText($promptFile, $codeContent, $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "```$([Environment]::NewLine)", $utf8NoBom)
    [System.IO.File]::AppendAllText($promptFile, "$([Environment]::NewLine)", $utf8NoBom)

    # Add change-specific review requirements
    $changedRequestTemplate = "## [GOALS] Change Review Requirements$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 1. **Change Impact Analysis**$([Environment]::NewLine)"
    $changedRequestTemplate += "- Analyze **impact of these changes on existing code**$([Environment]::NewLine)"
    $changedRequestTemplate += "- Evaluate impact on **related components or modules**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Database schema or API changes** compatibility issues$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 2. **Change Quality Review**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Quality assessment** of newly added code$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Correctness and efficiency** of modified logic$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Coding standards and conventions** compliance$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 3. **Risk Assessment**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **[HIGH RISK]**: Critical issues requiring immediate fixes$([Environment]::NewLine)"
    $changedRequestTemplate += "- **[MEDIUM RISK]**: Areas requiring careful monitoring$([Environment]::NewLine)"
    $changedRequestTemplate += "- **[LOW RISK]**: Improvements that would be beneficial$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 4. **Compatibility & Stability**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Existing API compatibility** maintenance$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Database migration** necessity$([Environment]::NewLine)"
    $changedRequestTemplate += "- Potential side effects from **dependency changes**$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 5. **Testing Strategy**$([Environment]::NewLine)"
    $changedRequestTemplate += "- Testing approaches for **newly added features**$([Environment]::NewLine)"
    $changedRequestTemplate += "- Identify areas requiring **regression testing**$([Environment]::NewLine)"
    $changedRequestTemplate += "- Suggest **integration testing** scenarios$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 6. **Deployment Considerations**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Pre-deployment checklist**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Rollback plan** necessity$([Environment]::NewLine)"
    $changedRequestTemplate += "- **Progressive deployment (Blue-Green, Canary)** requirements$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### 7. **Documentation Requirements**$([Environment]::NewLine)"
    $changedRequestTemplate += "- **API documentation** update necessity$([Environment]::NewLine)"
    $changedRequestTemplate += "- **User manual** changes$([Environment]::NewLine)"
    $changedRequestTemplate += "- Information requiring **team sharing**$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "## [CHECKLIST] Priority-Based Action Items$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "Please organize review results as follows:$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### Immediate Fixes Required (Critical)$([Environment]::NewLine)"
    $changedRequestTemplate += "- [ ] Critical issues that must be fixed$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### [HIGH] Pre-Deployment Fixes Recommended$([Environment]::NewLine)"
    $changedRequestTemplate += "- [ ] Improvements recommended before deployment$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "### [IMPROVEMENT] Future Improvements (Medium/Low)$([Environment]::NewLine)"
    $changedRequestTemplate += "- [ ] Items that can be improved when time allows$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "---$([Environment]::NewLine)$([Environment]::NewLine)"
    $changedRequestTemplate += "**[GOAL]**: System improvement through safe and high-quality code changes$([Environment]::NewLine)"

    [System.IO.File]::AppendAllText($promptFile, $changedRequestTemplate, $utf8NoBom)

    Write-ColorText "[SUCCESS] Changed code prompt generated successfully: $promptFile" "Green"
}

# Main execution
Write-ColorText "[INFO] Claude Code Review Prompt Generation (PowerShell)" "Cyan"

switch ($Mode) {
    "changed" {
        New-ChangedPrompt
    }
    default {
        New-BasicPrompt
    }
}

# Generate additional template files
$createTemplateScriptPath = Join-Path $ProjectRoot "code_review\scripts\create-prompt-templates.ps1"
if (Test-Path $createTemplateScriptPath) {
    & $createTemplateScriptPath
}

Write-ColorText "[SUCCESS] Prompt generation completed!" "Green"
Write-ColorText "[TIP] Copy contents of $OutputDir\review-prompt.txt to Claude." "Yellow"