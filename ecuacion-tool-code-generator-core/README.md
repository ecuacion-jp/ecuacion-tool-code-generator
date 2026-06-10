# ecuacion-tool-code-generator-core

## What is it?

`ecuacion-tool-code-generator-core` is the shared library module that contains the core code generation logic used by both `ecuacion-tool-code-generator-batch` and `ecuacion-tool-code-generator-web`.

This module is **not a standalone application**. It is a JAR dependency, not meant to be executed directly.

## What it contains

- Excel file reading logic
- Data validation and consistency checks
- Code generation logic (Entity, DAO, BL, Enum, config, etc.)
- Shared constants and configuration

## Used by

- [`ecuacion-tool-code-generator-batch`](../ecuacion-tool-code-generator-batch/) — command-line batch execution
- [`ecuacion-tool-code-generator-web`](../ecuacion-tool-code-generator-web/) — browser-based web execution

## Documentation

- [ecuacion-references](https://references.ecuacion.jp/ecuacion-references-tool-code-generator/public/en/article?id=home) — Official reference documentation
