# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Java Coding Rules

### Style Standards
- Follow **Google Java Style Guide** (enforced by Checkstyle in CI)
- Indentation: **2 spaces** (no tabs)
- Max line length: **100 characters** (excluding package/import statements) — **applies to comments and Javadoc as well**
- Encoding: **UTF-8**

### Imports
- Wildcard imports (`.*`) are **prohibited**
- Follow IDE auto-organization for import ordering

### Javadoc
- **Javadoc is required for all public classes, methods, and fields**
- When editing an existing file, review and update the Javadoc for any modified methods

### License Header
- All Java files must have an Apache 2.0 license header at the top
- Follow the same format as existing files

## File Creation / Editing Rules

- Before creating a new file, always review existing files in the same package
- When adding to a package that has a `package-info.java`, check its contents first

## Build Commands

```bash
# Build all modules (from root directory)
mvn compile

# Build individual modules
mvn -pl ecuacion-tool-code-generator-core compile
mvn -pl ecuacion-tool-code-generator-batch compile
mvn -pl ecuacion-tool-code-generator-web compile

# Code style check
mvn checkstyle:check

# Static analysis (all modules)
mvn spotbugs:check
```

**After editing any Java file, always run the following and fix any violations before finishing:**

```bash
# checkstyle (all modules)
mvn checkstyle:check

# spotbugs (all modules)
mvn spotbugs:check

# Javadoc generation check
mvn javadoc:javadoc
```

Common violations:
- Checkstyle: line length exceeding 100 characters (including comments and Javadoc)
- Checkstyle: missing Javadoc on public members
- Checkstyle: wildcard imports
- SpotBugs: private field access via reflection (use `protected` scope if necessary)

## Architecture Overview

A tool that reads DB/class specifications defined in Excel files and auto-generates Java source code for JPA Entities, DAOs, Business Logic, Spring configuration, etc.

### Module Structure

| Module | Packaging | Role |
|---|---|---|
| `ecuacion-tool-code-generator-core` | JAR | Core code generation engine |
| `ecuacion-tool-code-generator-batch` | JAR (Spring Boot Batch) | Batch execution interface |
| `ecuacion-tool-code-generator-web` | WAR (Spring Boot Web) | Web UI interface (Excel upload → ZIP download) |

### Processing Pipeline (core)

```
Excel file
  → ReadExcelFilesBlf (read Excel → convert to DTOs)
  → CheckAndComplementDataBlf (validation and complementing)
  → GenerationBlf (invoke each Generator to produce source code)
  → Output files
```

**Entry point:** `MainController.execute(inputDir, outputDir)`

The thread-safe generation context (system name, output destination, table info, etc.) is managed via `ThreadLocal<Info>`.

### Generator List (core: `generator/` package)

All Generators extend `AbstractGen` and are called collectively from `GenerationBlf`.

| Generator | Output |
|---|---|
| `EntityGen`, `EntityBodyGen` | JPA Entity |
| `BlGen` | Business Logic |
| `DaoGen`, `SqlPropertiesGen` | DAO + SQL properties |
| `EnumGen` | Enum |
| `ConfigGen`, `ConstantGen` | Configuration and constant classes |
| `AdviceGen` | Spring AOP Advice |
| `PropertiesFileGen` | Validation message properties file |
| `RecordGen`, `PerTableBaseRecordGen` | Record/DTO |
| `UtilGen`, `DataTypeGen` | Utility and data type classes |

### Batch Module

`BatchStarterTasklet` calls `MainController.execute()`.

- Input: `../ecuacion-tool-code-generator-batch/ecuacion-tool-code-generator-excel-format`
- Output: `./products/`

### Web Module

Flow: `SourceDownloadController` (`/public/sourceDownload`) → `SourceDownloadService`

1. Upload Excel file
2. Save temporarily to `{app.work-root-dir}/{timestamp}-{threadId}/inputExcel/`
3. Generate via `MainController.execute()`
4. Compress generated output to ZIP and provide for download

## Key Dependencies

- `ecuacion-splib` (spring base classes), `ecuacion-util-poi` (Excel processing), `ecuacion-lib` (common utilities)
- `zip4j` (ZIP generation, web module)
- `hsqldb` (in-memory DB for Spring Batch, batch module)
