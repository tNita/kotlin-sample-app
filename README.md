# オニオンアーキテクチャサンプル（書籍管理システム）

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
- テスト: `./gradlew test`
- DB / SQS 起動: `docker compose up -d postgres ministack`
- アプリ起動: `./gradlew bootRun`（DB が起動していること）
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

## TODO
- フォーマッター・リンターの導入
- 各層ごとのマルチプロジェクト構成の検討
- jOOQのKotlinコード自動生成設定
