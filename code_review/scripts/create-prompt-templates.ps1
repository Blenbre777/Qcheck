# =============================================================================
# Prompt Template Generation Script (PowerShell)
# =============================================================================
# Generates specialized prompt templates for various code review scenarios.
# =============================================================================

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

# Template directory creation
$TemplateDir = Join-Path $ProjectRoot "code_review\review-output\templates"
if (!(Test-Path $TemplateDir)) {
    New-Item -ItemType Directory -Path $TemplateDir -Force | Out-Null
}

Write-ColorText "[INFO] Generating specialized code review prompt templates (PowerShell)" "Cyan"

# 1. Security-focused review template
$securityContent = "# Security-Focused Code Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "## [GOAL] Security Review Perspective$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "Please perform a thorough review focused on **security vulnerabilities** for the following code.$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "### Key Review Items$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "#### 1. **Web Application Security**$([Environment]::NewLine)"
$securityContent += "- **SQL Injection**: PreparedStatement usage, dynamic query validation$([Environment]::NewLine)"
$securityContent += "- **XSS (Cross-Site Scripting)**: Input sanitization, output encoding$([Environment]::NewLine)"
$securityContent += "- **CSRF (Cross-Site Request Forgery)**: Token validation, SameSite cookies$([Environment]::NewLine)"
$securityContent += "- **Session Management**: Session hijacking, session fixation attack prevention$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "#### 2. **Authentication & Authorization**$([Environment]::NewLine)"
$securityContent += "- **Password Security**: Hashing algorithms, salt usage$([Environment]::NewLine)"
$securityContent += "- **Token Management**: JWT security, token expiration handling$([Environment]::NewLine)"
$securityContent += "- **Permission Verification**: Vertical/horizontal privilege escalation prevention$([Environment]::NewLine)"
$securityContent += "- **API Security**: Authentication headers, rate limiting$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "#### 3. **Data Protection**$([Environment]::NewLine)"
$securityContent += "- **Sensitive Information Exposure**: Information leakage in logs, error messages$([Environment]::NewLine)"
$securityContent += "- **Data Encryption**: Encryption at rest/in transit$([Environment]::NewLine)"
$securityContent += "- **Personal Information Processing**: GDPR, privacy law compliance$([Environment]::NewLine)"
$securityContent += "- **Data Validation**: Input type/length/format validation$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "### [CHECKLIST] Security Checklist$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "Please evaluate each item as [OK] Safe / [WARN] Caution / [FAIL] Risk:$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "- [ ] SQL query security$([Environment]::NewLine)"
$securityContent += "- [ ] User input validation$([Environment]::NewLine)"
$securityContent += "- [ ] Permission verification logic$([Environment]::NewLine)"
$securityContent += "- [ ] Session/token management$([Environment]::NewLine)"
$securityContent += "- [ ] Error information exposure$([Environment]::NewLine)"
$securityContent += "- [ ] File processing security$([Environment]::NewLine)"
$securityContent += "- [ ] Logging security$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "### Language Translation Request$([Environment]::NewLine)"
$securityContent += "**Please provide your entire security review response translated into Korean at the end.$([Environment]::NewLine)$([Environment]::NewLine)"
$securityContent += "{CODE_INSERTION_POINT}$([Environment]::NewLine)"

[System.IO.File]::WriteAllText("$TemplateDir\security-review-prompt.txt", $securityContent, $utf8NoBom)

# 2. Performance optimization template
$performanceContent = "# Performance Optimization-Focused Code Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "## [GOAL] Performance Improvement Perspective$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "Please perform detailed analysis from a **performance optimization** perspective for the following code.$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "### Key Review Items$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "#### 1. **Database Optimization**$([Environment]::NewLine)"
$performanceContent += "- **N+1 Problem**: Query optimization, lazy loading improvement$([Environment]::NewLine)"
$performanceContent += "- **Index Utilization**: Appropriate index design$([Environment]::NewLine)"
$performanceContent += "- **Batch Processing**: Batch Insert/Update utilization$([Environment]::NewLine)"
$performanceContent += "- **Connection Pooling**: Database connection management$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "#### 2. **Memory Management**$([Environment]::NewLine)"
$performanceContent += "- **Memory Leaks**: Resource release, connection pool management$([Environment]::NewLine)"
$performanceContent += "- **Caching Strategy**: Redis, memory cache utilization$([Environment]::NewLine)"
$performanceContent += "- **Object Lifecycle**: Unnecessary object creation prevention$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "#### 3. **Algorithm Efficiency**$([Environment]::NewLine)"
$performanceContent += "- **Time Complexity**: Big O notation analysis$([Environment]::NewLine)"
$performanceContent += "- **Data Structure Selection**: Appropriate data structure usage$([Environment]::NewLine)"
$performanceContent += "- **Loop Optimization**: Nested loop efficiency$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "### Language Translation Request$([Environment]::NewLine)"
$performanceContent += "**Please provide your entire performance review response translated into Korean at the end.$([Environment]::NewLine)$([Environment]::NewLine)"
$performanceContent += "{CODE_INSERTION_POINT}$([Environment]::NewLine)"

[System.IO.File]::WriteAllText("$TemplateDir\performance-review-prompt.txt", $performanceContent, $utf8NoBom)

# 3. Architecture review template
$architectureContent = "# Architecture Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "## [GOAL] System Architecture Review$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "Please analyze the following code from an **architectural design** perspective.$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "### Key Review Items$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "#### 1. **Layer Separation**$([Environment]::NewLine)"
$architectureContent += "- **Controller**: REST API design, input validation$([Environment]::NewLine)"
$architectureContent += "- **Service**: Business logic separation$([Environment]::NewLine)"
$architectureContent += "- **Repository**: Data access layer$([Environment]::NewLine)"
$architectureContent += "- **Entity**: Domain model design$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "#### 2. **Design Patterns**$([Environment]::NewLine)"
$architectureContent += "- **Dependency Injection**: IoC container utilization$([Environment]::NewLine)"
$architectureContent += "- **Strategy Pattern**: Algorithm abstraction$([Environment]::NewLine)"
$architectureContent += "- **Observer Pattern**: Event-driven architecture$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "#### 3. **Scalability & Maintainability**$([Environment]::NewLine)"
$architectureContent += "- **Loose Coupling**: Component independence$([Environment]::NewLine)"
$architectureContent += "- **High Cohesion**: Related functionality grouping$([Environment]::NewLine)"
$architectureContent += "- **Extensibility**: Future feature addition considerations$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "### Language Translation Request$([Environment]::NewLine)"
$architectureContent += "**Please provide your entire architecture review response translated into Korean at the end.$([Environment]::NewLine)$([Environment]::NewLine)"
$architectureContent += "{CODE_INSERTION_POINT}$([Environment]::NewLine)"

[System.IO.File]::WriteAllText("$TemplateDir\architecture-review-prompt.txt", $architectureContent, $utf8NoBom)

# 4. New feature review template
$newFeatureContent = "# New Feature Code Review Request$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "## [GOAL] New Feature Review$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "Please perform comprehensive review for the following **new feature**.$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "### Key Review Items$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "#### 1. **Feature Implementation**$([Environment]::NewLine)"
$newFeatureContent += "- **Requirement Fulfillment**: Business requirement reflection$([Environment]::NewLine)"
$newFeatureContent += "- **Exception Handling**: Error scenario response$([Environment]::NewLine)"
$newFeatureContent += "- **Data Validation**: Input value validity checks$([Environment]::NewLine)"
$newFeatureContent += "- **Edge Case Handling**: Boundary condition processing$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "#### 2. **Integration**$([Environment]::NewLine)"
$newFeatureContent += "- **Existing System Impact**: Compatibility with current features$([Environment]::NewLine)"
$newFeatureContent += "- **API Design**: RESTful design principles$([Environment]::NewLine)"
$newFeatureContent += "- **Database Changes**: Schema modification impact$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "#### 3. **Testing Strategy**$([Environment]::NewLine)"
$newFeatureContent += "- **Unit Test Coverage**: Test case completeness$([Environment]::NewLine)"
$newFeatureContent += "- **Integration Testing**: Cross-component testing$([Environment]::NewLine)"
$newFeatureContent += "- **User Acceptance Testing**: End-user perspective validation$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "### Language Translation Request$([Environment]::NewLine)"
$newFeatureContent += "**Please provide your entire new feature review response translated into Korean at the end.$([Environment]::NewLine)$([Environment]::NewLine)"
$newFeatureContent += "{CODE_INSERTION_POINT}$([Environment]::NewLine)"

[System.IO.File]::WriteAllText("$TemplateDir\new-feature-review-prompt.txt", $newFeatureContent, $utf8NoBom)

# 5. Windows usage guide
$usageGuideContent = "# Qcheck Code Review System Usage Guide (Windows)$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "## Quick Start$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Step 1: Open PowerShell$([Environment]::NewLine)"
$usageGuideContent += "- Search for 'PowerShell' in Windows Start Menu$([Environment]::NewLine)"
$usageGuideContent += "- Run **PowerShell** (no administrator privileges needed)$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Step 2: Navigate to Project Directory$([Environment]::NewLine)"
$usageGuideContent += "```powershell$([Environment]::NewLine)"
$usageGuideContent += "cd C:\path\to\your\Qcheck$([Environment]::NewLine)"
$usageGuideContent += "```$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Step 3: Execute Code Extraction$([Environment]::NewLine)"
$usageGuideContent += "**Full Code Review:**$([Environment]::NewLine)"
$usageGuideContent += "```powershell$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\code-review-all.bat$([Environment]::NewLine)"
$usageGuideContent += "```$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "**Changed Files Only:**$([Environment]::NewLine)"
$usageGuideContent += "```powershell$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\code-review-changed.bat$([Environment]::NewLine)"
$usageGuideContent += "```$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "**Security-Focused Review:**$([Environment]::NewLine)"
$usageGuideContent += "```powershell$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\code-review-security.bat$([Environment]::NewLine)"
$usageGuideContent += "```$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Step 4: Review Results$([Environment]::NewLine)"
$usageGuideContent += "1. Open `code_review\review-output\review-prompt.txt`$([Environment]::NewLine)"
$usageGuideContent += "2. Copy the entire content$([Environment]::NewLine)"
$usageGuideContent += "3. Paste into Claude AI for code review$([Environment]::NewLine)"
$usageGuideContent += "4. Save Claude's response to `code_review\review-output\claude-response.txt`$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "## Advanced Usage$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Direct PowerShell Execution$([Environment]::NewLine)"
$usageGuideContent += "```powershell$([Environment]::NewLine)"
$usageGuideContent += "# Extract all code$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\scripts\extract-code.ps1 -All$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "# Extract changed files only$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\scripts\extract-code.ps1 -Changed$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "# Extract changes since specific commit$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\scripts\extract-code.ps1 -Since HEAD~3$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "# Extract specific file patterns$([Environment]::NewLine)"
$usageGuideContent += ".\code_review\scripts\extract-code.ps1 -Files `"*.java`"$([Environment]::NewLine)"
$usageGuideContent += "```$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Configuration$([Environment]::NewLine)"
$usageGuideContent += "Edit `.code-review-config` file to customize:$([Environment]::NewLine)"
$usageGuideContent += "- Target directories$([Environment]::NewLine)"
$usageGuideContent += "- File extensions to include$([Environment]::NewLine)"
$usageGuideContent += "- Exclusion patterns$([Environment]::NewLine)"
$usageGuideContent += "- Maximum file size limits$([Environment]::NewLine)$([Environment]::NewLine)"
$usageGuideContent += "### Execution from Any Directory$([Environment]::NewLine)"
$usageGuideContent += "The scripts automatically detect the project root, so you can run them from:$([Environment]::NewLine)"
$usageGuideContent += "- Project root (`/Qcheck/`)$([Environment]::NewLine)"
$usageGuideContent += "- Backend directory (`/Qcheck/back/`)$([Environment]::NewLine)"
$usageGuideContent += "- Frontend directory (`/Qcheck/front/`)$([Environment]::NewLine)"
$usageGuideContent += "- Code review directory (`/Qcheck/code_review/`)$([Environment]::NewLine)$([Environment]::NewLine)"

[System.IO.File]::WriteAllText("$TemplateDir\usage-guide-windows.md", $usageGuideContent, $utf8NoBom)

Write-ColorText "[SUCCESS] PowerShell prompt templates generated successfully!" "Green"
Write-ColorText "[INFO] Generated templates:" "Cyan"
Write-Host "  - security-review-prompt.txt (Security-focused)"
Write-Host "  - performance-review-prompt.txt (Performance optimization)"
Write-Host "  - architecture-review-prompt.txt (Architecture)"
Write-Host "  - new-feature-review-prompt.txt (New features)"
Write-Host "  - usage-guide-windows.md (Windows usage guide)"