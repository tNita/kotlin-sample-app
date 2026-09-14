# Kotlin Backend サンプル（書籍管理システム）

## 技術スタック
- 言語/ランタイム: Kotlin 2.2.x / JDK 21
- フレームワーク: Spring Boot 4.0.x
- ビルド: Gradle（Kotlin DSL）
- ORM: jOOQ
- マイグレーション: Flyway
- テスト: JUnit 5
- データベース: PostgreSQL

## アーキテクチャ概要

[architecture.md](docs/architecture.md)に記載

## コーディングルール
[coding-rule.md](docs/coding-rule.md)に記載

## 起動・テスト方法
- 前提: JDK 21、接続先の PostgreSQL・AWS サービス（ローカル環境の構築には任意で Docker を利用可能）
- 単体テスト: `./gradlew test`
- 統合テスト: `./gradlew intTest`
- コードスタイル検査: `./gradlew ktlintCheck`
- 自動整形: `./gradlew ktlintFormat`
- DB / SQS 起動: `docker compose up -d postgres ministack`
- アプリ起動: `./gradlew :infra:bootRun`（DB が起動していること）
- Docker で全体起動: `docker compose --profile app up --build`
- Swagger UI: アプリ起動後、`http://localhost:8080/swagger-ui/index.html` にアクセス
- MiniStack: `http://localhost:4566`

## TODO: 
- support authn / authz.
