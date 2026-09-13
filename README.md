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

## 書籍一括更新バッチの起動

API とバッチは同じ `infra` モジュール内の別エントリポイントで起動します。
API は REST・著者登録 SQS リスナー・共通設定を読み込みます。
バッチは Runner・共通設定を読み込み、Web サーバーを起動しません。
それぞれコンポーネントスキャンの範囲を限定し、互いの起動設定や入力処理を読み込まない構成です。
`BOOK_UPDATE_BATCH_ENABLED` による切り替えは廃止しました。

```sh
# API
./gradlew :infra:bootRun
# バッチ
./gradlew :infra:bootRunBatch
```

バッチの DB と AWS 接続設定は API と共通の環境変数を利用します。
API・バッチの起動に Docker は不要です。DB・外部サービスとキューは事前に用意し、環境変数で接続先を指定してください。
`bookmanager-book-update-batch` キューから最大１件を取得して処理します。
正常終了（20 秒の受信待機で対象なしの場合を含む）は終了コード `0`、処理失敗はログを出力して `1` を返します。
JVM の終了はバッチ専用の main 関数で行います。

実行可能 JAR もそれぞれ生成できます。終了コードを直接取得する場合は JAR を実行してください。

```sh
./gradlew :infra:bootJar :infra:bootJarBatch
java -jar infra/build/libs/infra-0.0.1-SNAPSHOT.jar
java -jar infra/build/libs/infra-0.0.1-SNAPSHOT-batch.jar
```
