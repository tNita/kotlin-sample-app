# Kotlin Backend サンプル（書籍管理システム）

## 技術スタック
- 言語/ランタイム: Kotlin 2.2.x / JDK 21
- フレームワーク: Spring Boot 4.0.x
- ビルド: Gradle（Groovy DSL）
- ORM: jOOQ
- マイグレーション: Flyway
- テスト: JUnit 5
- データベース: PostgreSQL

## アーキテクチャ概要

[architecture.md](docs/architecture.md)に記載

## コーディングルール
[coding-rule.md](docs/coding-rule.md)に記載

## 起動・テスト方法
- 前提: JDK 21、Docker (PostgreSQL / SQS を起動する場合)
- 単体テスト: `./gradlew test`
- 統合テスト: `./gradlew intTest`
- コードスタイル検査: `./gradlew ktlintCheck`
- 自動整形: `./gradlew ktlintFormat`
- DB / SQS 起動: `docker compose up -d postgres ministack`
- アプリ起動: `./gradlew :infra:bootRun`（DB が起動していること）
- Docker で全体起動: `docker compose --profile app up --build`
- Swagger UI: アプリ起動後、`http://localhost:8080/swagger-ui/index.html` にアクセス
- MiniStack: `http://localhost:4566`

## メッセージキューでの著者登録

アプリケーションは SQS の `bookmanager-author-registration` キューを購読し、メッセージを受け取ると著者を登録します。ローカル環境では MiniStack の SQS を使用します。

キューはアプリケーションから作成しません。実環境では Terraform / CDK / CloudFormation などで事前に用意してください。ローカルで動作確認する場合のみ、MiniStack にキューを作成します。

- Queue: `bookmanager-author-registration`
- Payload:

```json
{
  "name": "宮沢賢治",
  "birthDate": "1896-08-27"
}
```

ローカル確認用にキューを作成する例:

```sh
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test AWS_DEFAULT_REGION=ap-northeast-1 \
aws --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name bookmanager-author-registration
```

AWS CLI から投入する例:

```sh
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test AWS_DEFAULT_REGION=ap-northeast-1 \
aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://localhost:4566/000000000000/bookmanager-author-registration \
  --message-body '{"name":"宮沢賢治","birthDate":"1896-08-27"}'
```

## マルチプロジェクト構成

- `shared`: 複数層で共有する基盤的な型
- `domain`: エンティティ、値オブジェクト、不変条件
- `application`: ユースケースと入出力ポート
- `infra`: Spring Boot アプリケーション、REST/SQS、jOOQ、外部サービス連携

依存方向は `infra → application → domain → shared` です。実行可能なアプリケーションと統合テストは `infra` に配置しています。

`infra` の単体テストと統合テストは、それぞれ `testImplementation` と `intTestImplementation` で依存関係を分離しています。統合テストは単体テストの依存を継承しません。

## ライブラリバージョン管理

ライブラリと Gradle プラグインのバージョンは、Gradle 標準の Version Catalog である [`gradle/libs.versions.toml`](./gradle/libs.versions.toml) に集約しています。依存関係を追加・更新する際は、このファイルでバージョンを管理し、各モジュールでは `libs` エイリアスを利用してください。
