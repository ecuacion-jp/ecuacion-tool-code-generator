# ecuacion-tool-code-generator-web

## What is it?

`ecuacion-tool-code-generator-web` is the browser-based execution module of the code generator.
It accepts DB/class specification Excel files uploaded from a browser and returns the generated Java source code as a ZIP file for download.

The artifact is an executable WAR file with an embedded Tomcat server. No external servlet container is needed.

## How to Run

```bash
java -jar ecuacion-tool-code-generator-web-x.x.x.war
```

Open `http://localhost:8080` in a browser.

## Documentation

For setup, quick start, and configuration details, see the official reference:

- [ecuacion-references](https://references.ecuacion.jp/ecuacion-references-tool-code-generator/public/ja/article?id=code-generator-web/overview) — Official reference documentation
