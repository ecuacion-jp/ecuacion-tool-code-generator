# ecuacion-tool-code-generator

[![Java CI](https://github.com/ecuacion-jp/ecuacion-tool-code-generator/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ecuacion-jp/ecuacion-tool-code-generator/actions/workflows/ci.yml)
[![GitHub Release](https://img.shields.io/github/v/release/ecuacion-jp/ecuacion-tool-code-generator)](https://github.com/ecuacion-jp/ecuacion-tool-code-generator/releases)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## What is it?

`ecuacion-tool-code-generator` reads DB item definition files (Excel format) and auto-generates Java source code for Spring Boot + JPA applications.

**What is generated:**

- JPA Entity classes
- DAO classes and SQL property files
- Business Logic classes
- Enum classes
- Spring configuration and constant classes
- Validation message property files
- and more

Two execution modes are available:

- **Batch mode** — Run from the command line by pointing to an input directory
- **Web mode** — Upload Excel files via browser and download the generated source as a ZIP

## Versioning

This project follows the spirit of [Semantic Versioning](https://semver.org/). Major version increments indicate breaking changes.

## System Requirements

- JDK 21 or above

## Documentation

- [ecuacion-references](https://references.ecuacion.jp/ecuacion-references-tool-code-generator/public/en/article?id=home) — Official reference documentation

## Download & Usage

Download the latest release from [GitHub Releases](https://github.com/ecuacion-jp/ecuacion-tool-code-generator/releases).

### Batch mode

```bash
java -jar ecuacion-tool-code-generator-batch-x.x.x.jar
```

Input Excel files are read from `./excel-format/` (relative to the working directory).  
Generated sources are written to `./products/`.

### Web mode

Deploy `ecuacion-tool-code-generator-web-x.x.x.war` to a servlet container (e.g. Tomcat), then open the application in a browser to upload Excel files and download the generated ZIP.

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for how to report bugs, suggest features, and submit pull requests.
