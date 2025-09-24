# 🔍 Claude Automated Code Review System

A prompt-based automated code review system utilizing Claude AI.

## 🚀 Quick Start

### 🪟 Windows Environment (Primary)

#### 🎯 Simple Execution (Recommended)
```cmd
REM Changed files review
code-review-changed.bat

REM Full code review
code-review-all.bat

REM Security-focused review
code-review-security.bat

REM Performance optimization review
code-review-performance.bat

REM Initial setup
setup-code-review.bat
```

#### 💻 Direct PowerShell Execution
```powershell
# Full code review
.\code_review\scripts\extract-code.ps1 -All

# Changed files only
.\code_review\scripts\extract-code.ps1 -Changed

# Changes since specific commit
.\code_review\scripts\extract-code.ps1 -Since HEAD~3

# Specific file patterns
.\code_review\scripts\extract-code.ps1 -Files "*.java"
```

### 🐧 Linux/Mac Environment

#### 1. Changed Files Review
```bash
# Review only recently changed code
./code_review/scripts/extract-code.sh --changed
```

#### 2. Full Code Review
```bash
# Review entire codebase
./code_review/scripts/extract-code.sh --all
```

#### 3. Specific Files Only
```bash
# Java files only
./code_review/scripts/extract-code.sh --files "*.java"

# TypeScript files only
./code_review/scripts/extract-code.sh --files "*.ts"
```

## ⚙️ Configuration

### Configuration File: `.code-review-config`

```bash
# Target directories (relative to project root)
TARGET_DIRS="back/src front/src"

# File extensions to include
INCLUDE_EXTENSIONS="java ts js html scss css xml properties json yaml yml"

# Exclusion patterns
EXCLUDE_PATTERNS="node_modules target .git *.min.js *.map build dist coverage"

# Maximum file size (KB)
MAX_FILE_SIZE=102400

# Output directory
OUTPUT_DIR="review-output"
```

### Path Resolution

The system automatically detects the project root directory, allowing execution from anywhere within the project:

- **Project root**: `/Qcheck/`
- **Backend directory**: `/Qcheck/back/`
- **Frontend directory**: `/Qcheck/front/`
- **Code review directory**: `/Qcheck/code_review/`

All paths in configuration are relative to the project root.

## 📁 Directory Structure

```
Qcheck/
├── code_review/                    # Code review system
│   ├── scripts/                    # Extraction scripts
│   │   ├── extract-code.ps1        # Main extraction script (PowerShell)
│   │   ├── generate-prompt.ps1     # Prompt generation (PowerShell)
│   │   ├── create-prompt-templates.ps1  # Template generation
│   │   └── *.sh                    # Linux/Mac scripts
│   ├── review-output/              # Generated output
│   │   ├── review-prompt.txt       # Final prompt for Claude
│   │   ├── code-to-review.txt      # Extracted code
│   │   ├── file-list.txt           # List of included files
│   │   └── templates/              # Specialized templates
│   ├── code-review-*.bat           # Windows batch files
│   ├── .code-review-config         # Configuration file
│   └── README-code-review.md       # This file
└── review-output/                  # Output directory (auto-created)
    ├── review-prompt.txt           # Claude prompt
    ├── code-to-review.txt          # Extracted code
    ├── file-list.txt               # File list
    ├── diff-summary.txt            # Git diff (for changed files)
    └── templates/                  # Specialized prompt templates
```

## 🔄 Workflow

### Standard Workflow

1. **Execute Script**
   ```cmd
   # Windows
   code-review-changed.bat

   # PowerShell
   .\code_review\scripts\extract-code.ps1 -Changed
   ```

2. **Review Generated Files**
   - `review-output\review-prompt.txt` - Copy this to Claude
   - `review-output\code-to-review.txt` - Extracted source code
   - `review-output\file-list.txt` - List of included files

3. **Submit to Claude**
   - Copy contents of `review-prompt.txt`
   - Paste into Claude AI interface
   - Request code review

4. **Save Results**
   - Save Claude's response to `review-output\claude-response.txt`
   - Apply suggested improvements
   - Re-run review as needed

### Specialized Reviews

#### Security Review
```cmd
code-review-security.bat
```
Generates security-focused prompts checking for:
- SQL injection vulnerabilities
- XSS attacks
- Authentication/authorization issues
- Input validation problems

#### Performance Review
```cmd
code-review-performance.bat
```
Generates performance-focused prompts analyzing:
- N+1 query problems
- Algorithm efficiency
- Memory usage optimization
- Database query optimization

## 🛠️ Advanced Usage

### Custom File Patterns
```powershell
# Review only Java files
.\code_review\scripts\extract-code.ps1 -Files "*.java"

# Review specific directories
# (Modify TARGET_DIRS in .code-review-config)
```

### Historical Changes
```powershell
# Changes since 3 commits ago
.\code_review\scripts\extract-code.ps1 -Since HEAD~3

# Changes since specific commit
.\code_review\scripts\extract-code.ps1 -Since abc123
```

### Large Codebases
For large projects, consider:
1. **Partial Reviews**: Use `-Files` parameter for specific file types
2. **Changed Files Only**: Use `-Changed` for focused reviews
3. **Directory-Specific**: Modify `TARGET_DIRS` in configuration

## 🔧 Troubleshooting

### Common Issues

#### PowerShell Execution Policy
```powershell
# Check current policy
Get-ExecutionPolicy

# Set policy for current user
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### Git Repository Required
- Ensure the project is a Git repository for changed file detection
- Use `-All` parameter if Git is not available

#### File Size Limits
- Adjust `MAX_FILE_SIZE` in `.code-review-config`
- Consider excluding large generated files in `EXCLUDE_PATTERNS`

### Manual Execution
If automated scripts fail:
```powershell
# Manual PowerShell execution with full path resolution
powershell -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '.\code_review\scripts\extract-code.ps1' -All"
```

## 📊 Output Files

| File | Description |
|------|-------------|
| `review-prompt.txt` | Complete prompt ready for Claude AI |
| `code-to-review.txt` | All extracted source code |
| `file-list.txt` | List of included files with paths |
| `diff-summary.txt` | Git diff information (changed files only) |
| `claude-response.txt` | Save Claude's response here (manual) |

## 🎯 Best Practices

1. **Regular Reviews**: Run changed file reviews frequently
2. **Full Reviews**: Periodic full codebase reviews
3. **Specialized Reviews**: Use security/performance templates for focused analysis
4. **Documentation**: Save Claude's responses for reference
5. **Iterative Improvement**: Apply suggestions and re-review

## 🌟 Features

- ✅ **Universal Path Resolution**: Run from any project directory
- ✅ **Multi-Language Support**: Java, TypeScript, JavaScript, HTML, SCSS, etc.
- ✅ **Git Integration**: Automatic changed file detection
- ✅ **Specialized Templates**: Security, performance, architecture focus
- ✅ **Configurable**: Customizable file patterns and exclusions
- ✅ **Cross-Platform**: Windows (PowerShell) and Linux/Mac (Bash) support
- ✅ **UTF-8 Support**: Proper encoding handling for international characters

## 📝 Notes

- All Korean text has been translated to English for international accessibility
- The system maintains compatibility with existing workflows
- Configuration files use relative paths for portability
- Scripts automatically detect and adapt to the project structure