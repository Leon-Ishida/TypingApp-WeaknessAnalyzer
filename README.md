# TypingApp — タイピングミス分析アプリケーション

タイピングテストの結果からミスの傾向を分析し、弱点に特化した練習問題を自動生成するアプリケーション

## クイックスタート

### 前提環境
- Java 21+ (検証環境: 23.0.1)
- Maven 3.9+ (検証環境: 3.9.11)

### 起動コマンド
```bash
mvn spring-boot:run
```

### ブラウザアクセス
- Webアプリ: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### 最速フロー
1. トップ画面からテストを開始する
2. 10問のテストを行い、入力結果を提出する
3. WPM・正答率・ミス傾向を確認する
4. 弱点分析に基づく練習問題を開始する

### 動作確認の目安
`http://localhost:8080`でテスト開始画面が表示されれば起動成功

## 解決する課題

一般的なタイピング練習ソフトはWPM（1分あたりの入力単語数）を測定するだけで、**なぜミスをしたのか**を分析しない

そのため、同じミスを繰り返す非効率な練習になりがちである

本アプリは、タイピングミスを**置換・交換・削除・挿入**の4種類に分類し、最も頻出するミスパターンに基づいた練習単語を提供することで、効率的なタイピング能力の向上を支援する

## コアアルゴリズム

### ダメラウ・レーベンシュタイン距離によるミス判定

ミスタイプの検出には**ダメラウ・レーベンシュタイン距離**を採用した

| 検討した手法 | 採用/不採用 | 理由 |
|---|---|---|
| ダメラウ・レーベンシュタイン距離 | 採用 | 4種のミス（置換・交換・削除・挿入）をすべて検出可能 |
| レーベンシュタイン距離 | 不採用 | 交換ミスを検出できない |
| 機械学習 | 不採用 | 判定根拠が不透明で、ユーザーにミスの理由を説明できない |

- **計算量**:
    - $O(N \times M)$（$N, M$は単語の文字数）
    - 入力文字数が少ないためリアルタイム処理でも問題なし
- **ミス判定の優先順位**: 置換 > 交換 > 削除 > 挿入（実際のタイプミスの頻度に基づく）

## 技術スタック

| カテゴリ | 技術 |
|---|---|
| 言語 | Java |
| フレームワーク | Spring Boot |
| データベース | H2(JPA) |
| APIドキュメント | Swagger UI(SpringDoc) |
| テスト | JUnit 5, Mockito |
| データ処理 | Jackson (JSON) |
| ビルド | Maven |

## 開発の経緯
当初は開発の容易さおよびローカル環境だけで完結できることから、JavaFXでGUIアプリとして制作していた

しかし、このアプリの利用者はタイピングが苦手なパソコン初心者であることを考えると、環境構築が必要なJavaFX版では利用されにくいと考えた

そのため、配布・更新がしやすく、環境構築なしで利用できるSpring Boot版へ移行した

現在は、テスト &rarr; 結果確認 &rarr; 練習という一連の流れをWeb上で体験できる構成として実装している

## アーキテクチャと設計判断

### データモデル — `record` 型の採用

`TestResult`, `WordResult`, `MistakeDetail` などのデータ保持クラスに Java の `record` 型を採用

- **不変性**: データが作成後に意図せず書き換えられることを防ぎ、バグの混入リスクを減少
- **可読性**: getter, `equals`, `hashCode` などの定型コードを自動生成

### データ構造の選定

- **`LinkedHashMap`** (`TypingAnalyzer`)
    - 単語とミス情報を紐づけつつ、入力の時系列順を保持するため
- **`Set`** (`PracticeService`)
    - 練習単語の重複排除をデータ構造で担保するため
    - 平均的な状況で、`List` + `contains` ($O(N)$) より `Set` ($O(1)$) が高速
- **Stream API** (`WeaknessAnalyzer`, `PracticeService`)
    - `groupingBy`, `counting`, `sorted`, `limit`などにより、集計処理が簡潔に表現できるため
    - 状態遷移を追う負担が減るため

### データベース設計

#### 設計方針
- 本アプリでは、テスト結果の保存にH2 Databaseを用いている
- ORMはSpring Data JPA
- 初期段階では単一テーブルで実装していたが、正規化することで検索性と拡張性を持たせた
- テーブル名はエンティティのTable指定に合わせている

#### 主要テーブル
- **[test_result](src/main/java/application/entity/TestResultEntity.java)** 1件のテスト結果を表す
    - 主なカラム: `id`, `timestamp`, `wpm`, `accuracy`
- **[word_result](src/main/java/application/entity/WordResultEntity.java)** 1件のテスト内の各単語の結果
    - 主なカラム: `id`, `test_result_id(FK)`, `word`, `answer`
- **[mistake_detail](src/main/java/application/entity/MistakeDetailEntity.java)** 単語ごとのミス詳細
    - 主なカラム: `id`, `word_result_id(FK)`, `mistake_type`, `expected`, `actual`, `insertion`

#### リレーション
- `test_result` 1 : N `word_result`
- `word_result` 1 : N `mistake_detail`
- これによりテスト単位 &rarr; 単語単位 &rarr; ミス単位で段階的に分析できる構造としている

#### 正規化の理由
`results`をJSON文字列で保存する方式から正規化した理由として以下の3つの点が存在する
- ミスタイプ別の抽出や将来の集計クエリを実行しやすい
- 部分更新・条件検索の拡張に対応しやすい
- データ構造が明示的になり、保守しやすい

#### 現在の前提運用
- `jdbc:h2:mem`を利用しており、再起動で初期化される(設定は[application.properties](src/main/resources/application.properties)を参照)
- 本レポジトリの目的は学習であるため、まずは機能検証のしやすさを優先している

#### 今後の拡張予定
- 再起動後も保持されるデータベースへの移行によるテスト結果の蓄積
- 過去結果の直接閲覧機能:
    - `test_result.timestamp`を起点に一覧取得、期間フィルタを実装予定
- 過去結果から練習問題生成:
    - 永続DB移行後、蓄積された`mistake_detail`を集計し頻出ミスパターンを抽出して出題に反映予定
- マルチユーザー対応

### UIとロジックの分離

コントローラ層 &rarr; サービス層 &rarr; ロジック層のレイヤードアーキテクチャを採用し、HTTPリクエスト処理とビジネスロジックを分離

ロジック層は特定のフレームワークに依存せず、単体テストが容易な構造を実現

### 主要クラスの責務

| クラス | レイヤー | 責務 |
|---|---|---|
| [MistakeAnalyzer](src/main/java/application/util/MistakeAnalyzer.java) | ロジック | ダメラウ・レーベンシュタイン距離によるミス検出 |
| [TypingAnalyzer](src/main/java/application/typingtest/TypingAnalyzer.java) | ロジック | テスト結果の集約（WPM・正答率計算） |
| [WeaknessAnalyzer](src/main/java/application/service/WeaknessAnalyzer.java) | ロジック | ミスパターンの頻度分析・弱点抽出 |
| [PracticeService](src/main/java/application/service/PracticeService.java) | サービス | 弱点に基づく練習単語の生成 |
| [StatisticsCalculator](src/main/java/application/result/StatisticsCalculator.java) | ロジック | 期間別の統計計算（平均WPM・正答率） |
| [ResultFilter](src/main/java/application/result/ResultFilter.java) | ロジック | テスト結果の期間フィルタリング（今日・今週・今月） |
| [AnalyzerController](src/main/java/application/controller/AnalyzerController.java) | コントローラ | RESTエンドポイントの定義、HTTPリクエスト/レスポンスの処理 |
| [AnalyzeService](src/main/java/application/service/AnalyzeService.java) | サービス | APIリクエストの処理、Logic層の呼び出し、EntityとDTOの相互変換 |
| [TestResultEntity](src/main/java/application/entity/TestResultEntity.java) | エンティティ | テスト結果のDB永続化、`TestResult`との相互変換 |
| [TestResultRepository](src/main/java/application/repository/TestResultRepository.java) | レポジトリ | JPAによるDBアクセス |

## API仕様
| メソッド | パス | 機能 |
|---|---|---|
| POST | `/api/analyze` | `MistakeAnalyzer`で正誤判定し、JSONで判定結果を返す |
| GET | `/api/results` | 過去の履歴を返す |
| GET | `/api/results/{id}` | 指定したidを持つ過去の履歴を返す |
| GET | `/api/test/start` | テストの問題となる単語のリストとテスト開始時刻を返す |
| POST | `/api/test/submit` | ユーザーの入力結果を送り、結果と統計を返す |
| GET | `/api/practice/weakness` | 直近のテスト結果から各ミスの上位傾向を返す |
| GET | `/api/practice/start` | 弱点単語リストと頻出ミス単語リストを返す |

## 品質保証

JUnit 5 による単体テストを整備し、リファクタリングの安全網を構築している

| テストクラス | 主なテスト内容 |
|---|---|
| [MistakeAnalyzerTest](src/test/java/application/util/MistakeAnalyzerTest.java) | 4種のミス検出、複合ミス、空文字、重複文字 |
| [TypingAnalyzerTest](src/test/java/application/typingtest/TypingAnalyzerTest.java) | WPM・正答率の計算検証、null/空入力の例外処理 |
| [WeaknessAnalyzerTest](src/test/java/application/service/WeaknessAnalyzerTest.java) | 頻出ミスの抽出、ソート・件数制限、null/空入力 |
| [PracticeServiceTest](src/test/java/application/service/PracticeServiceTest.java) | 弱点単語の生成、サイズ制限、全問正解時の挙動 |
| [StatisticsCalculatorTest](src/test/java/application/result/StatisticsCalculatorTest.java) | 平均計算、丸め処理、境界値 |
| [ResultFilterTest](src/test/java/application/result/ResultFilterTest.java) | 日付境界値、うるう年、年末年始をまたぐ週 |

### テスト手法
- **パラメタライズドテスト**: 同一ロジックに対する複数パターンの効率的な検証
- **境界値テスト**: 日付の `0:00:00.000` / `23:59:59.999` など厳密な境界条件
- **Mockito**: 外部依存（`WordManager`）のモック化による単体テストの独立性確保

## 既知の制限
- H2データベースのため、再起動で記録が消える
- 練習結果を保存しない設計にしている

## 設計判断の記録

技術選定やアーキテクチャの意思決定は [ADR.md](ADR.md) に記録している

## 今後の計画

- **Spring Boot REST API への移行**: 2026/03/21 完了
- **タイピングテスト・弱点分析・練習機能の追加**: 2026/04/22 完了
- **再起動後も保持されるDBへの移行**
- **過去結果の直接閲覧機能**
- **過去結果に基づく練習問題生成**
- **マルチユーザー対応**
