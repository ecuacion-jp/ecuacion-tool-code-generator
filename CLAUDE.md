# ecuacion-tool-code-generator - Claude Code Guidelines

## Internal Documentation

Read `ecuacion-internal-docs` repository: `CLAUDE.md` before working in this project.
It covers common guidelines, project-specific SPEC.md, and local setup checks.

## Project Overview

A code generator tool that reads DB/class specifications from Excel files and auto-generates Java source code (JPA Entity, DAO, Business Logic, Spring config, etc.) for Spring Boot applications. Multi-module Maven project.

- **Java**: 21
- **Build tool**: Maven (multi-module)
- **Main modules**: `ecuacion-tool-code-generator-core`, `ecuacion-tool-code-generator-batch`, `ecuacion-tool-code-generator-web`
