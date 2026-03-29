# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Java コーディングルール

### スタイル基準
- **Google Java Style Guide** に従う（CIでCheckstyleにより強制）
- インデント: **スペース2つ**（タブ禁止）
- 最大行長: **100文字**（package/import文を除く）— **コメント・Javadocも対象**
- エンコーディング: **UTF-8**

### Import
- ワイルドカードインポート（`.*`）は**禁止**
- IDEの自動整理に従って並び順を整える

### Javadoc
- **全publicクラス・メソッド・フィールドにJavadocが必要**
- 既存ファイルを編集した場合、変更したメソッドのJavadocも見直して更新する

### ライセンスヘッダー
- 全Javaファイルの先頭にApache 2.0ライセンスヘッダーが必要
- 既存ファイルと同じフォーマットに従う

## ファイル作成・編集ルール

- 新規ファイルを作成する前に、同パッケージの既存ファイルを必ず参照する
- `package-info.java` が存在するパッケージに追加する場合、その内容を先に確認する

## ビルドコマンド

```bash
# 全モジュールをビルド（ルートディレクトリから）
mvn compile

# 個別モジュールのビルド
mvn -pl ecuacion-tool-code-generator-core compile
mvn -pl ecuacion-tool-code-generator-batch compile
mvn -pl ecuacion-tool-code-generator-web compile

# コードスタイルチェック
mvn checkstyle:check

# 静的解析（全モジュール）
mvn spotbugs:check
```

**Javaファイルを編集した後は必ず以下を実行し、違反があれば修正してから完了とする:**

```bash
# checkstyle（全モジュール）
mvn checkstyle:check

# spotbugs（全モジュール）
mvn spotbugs:check

# Javadoc生成確認
mvn javadoc:javadoc
```

よくある違反:
- Checkstyle: 行長100文字超（コメント・Javadoc含む）
- Checkstyle: publicメンバーへのJavadoc漏れ
- Checkstyle: ワイルドカードインポート
- SpotBugs: リフレクションによるprivateフィールドアクセス（必要なら `protected` スコープで対処）

## アーキテクチャ概要

Excelファイルで定義されたDB/クラス仕様を読み込み、JPA Entity・DAO・Business Logic・Spring設定等のJavaソースコードを自動生成するツール。

### モジュール構成

| モジュール | パッケージング | 役割 |
|---|---|---|
| `ecuacion-tool-code-generator-core` | JAR | コード生成エンジン本体 |
| `ecuacion-tool-code-generator-batch` | JAR (Spring Boot Batch) | バッチ実行インターフェース |
| `ecuacion-tool-code-generator-web` | WAR (Spring Boot Web) | Web UIインターフェース（Excelアップロード→ZIPダウンロード） |

### 処理パイプライン (core)

```
Excelファイル
  → ReadExcelFilesBlf（Excel読み込み → DTO変換）
  → CheckAndComplementDataBlf（バリデーション・補完）
  → GenerationBlf（各Generatorを呼び出してソースコード生成）
  → 出力ファイル
```

**起点クラス:** `MainController.execute(inputDir, outputDir)`

スレッドセーフな生成コンテキスト（システム名・出力先・テーブル情報等）は `ThreadLocal<Info>` で管理される。

### Generator一覧 (core: `generator/` パッケージ)

全Generatorは `AbstractGen` を継承し、`GenerationBlf` からまとめて呼び出される。

| Generator | 生成物 |
|---|---|
| `EntityGen`, `EntityBodyGen` | JPA Entity |
| `BlGen` | Business Logic |
| `DaoGen`, `SqlPropertiesGen` | DAO + SQLプロパティ |
| `EnumGen` | Enum |
| `ConfigGen`, `ConstantGen` | 設定・定数クラス |
| `AdviceGen` | Spring AOP Advice |
| `PropertiesFileGen` | バリデーションメッセージ用propertiesファイル |
| `RecordGen`, `PerTableBaseRecordGen` | Record/DTO |
| `UtilGen`, `DataTypeGen` | ユーティリティ・データ型クラス |

### Batch モジュール

`BatchStarterTasklet` が `MainController.execute()` を呼び出す。

- 入力: `../ecuacion-tool-code-generator-batch/ecuacion-tool-code-generator-excel-format`
- 出力: `./products/`

### Web モジュール

`SourceDownloadController` (`/public/sourceDownload`) → `SourceDownloadService` の流れ。

1. Excelファイルアップロード
2. `{app.work-root-dir}/{timestamp}-{threadId}/inputExcel/` に一時保存
3. `MainController.execute()` で生成
4. 生成結果をZIP圧縮してダウンロード提供

## 主要な依存ライブラリ

- `ecuacion-splib` (spring base classes), `ecuacion-util-poi` (Excel処理), `ecuacion-lib` (共通ユーティリティ)
- `zip4j` (ZIP生成、webモジュール)
- `hsqldb` (Spring Batch用インメモリDB、batchモジュール)
