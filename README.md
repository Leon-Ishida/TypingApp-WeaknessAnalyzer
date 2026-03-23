# TypingApp — タイピングミス分析アプリケーション

タイピングテストの結果からミスの傾向を分析し、弱点に特化した練習問題を自動生成するアプリケーション。

## 解決する課題

一般的なタイピング練習ソフトはWPM（1分あたりの入力単語数）を測定するだけで、**なぜミスをしたのか**を分析しない。そのため、同じミスを繰り返す非効率な練習になりがちである。

本アプリは、タイピングミスを**置換・交換・削除・挿入**の4種類に分類し、最も頻出するミスパターンに基づいた練習単語を提供することで、効率的なタイピング能力の向上を支援する。

## コアアルゴリズム

### ダメラウ・レーベンシュタイン距離によるミス判定

ミスタイプの検出には**ダメラウ・レーベンシュタイン距離**を採用した。

| 検討した手法 | 採用/不採用 | 理由 |
|---|---|---|
| ダメラウ・レーベンシュタイン距離 | 採用 | 4種のミス（置換・交換・削除・挿入）をすべて検出可能 |
| レーベンシュタイン距離 | 不採用 | 交換ミスを検出できない |
| 機械学習 | 不採用 | 判定根拠が不透明で、ユーザーにミスの理由を説明できない |

- **計算量**: $O(N \times M)$（$N, M$は単語の文字数）。入力文字数が少ないためリアルタイム処理でも問題なし
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

## アーキテクチャと設計判断

### データモデル — `record` 型の採用

`TestResult`, `WordResult`, `MistakeDetail` などのデータ保持クラスに Java の `record` 型を採用。

- **不変性**: データが作成後に意図せず書き換えられることを防ぎ、バグの混入リスクを減少
- **可読性**: getter, `equals`, `hashCode` などの定型コードを自動生成

### データ構造の選定

- **`LinkedHashMap`**（`TypingAnalyzer`）: 単語とミス情報を紐づけつつ、入力の時系列順を保持
- **`Set`**（`PracticeService`）: 練習単語の重複排除をデータ構造で担保。`List` + `contains` ($O(N)$) より `Set` ($O(1)$) が高速
- **Stream API**（`WeaknessAnalyzer`, `PracticeService`）: 将来的なデータ量増大時の並列化に対応

### UIとLogicの分離

Controller → Service → Logic層のレイヤードアーキテクチャを採用し、HTTPリクエスト処理とビジネスロジックを分離。Logic層は特定のフレームワークに依存せず、単体テストが容易な構造を実現

### 主要クラスの責務

| クラス | レイヤー | 責務 |
|---|---|---|
| `MistakeAnalyzer` | Logic | ダメラウ・レーベンシュタイン距離によるミス検出 |
| `TypingAnalyzer` | Logic | テスト結果の集約（WPM・正答率計算） |
| `WeaknessAnalyzer` | Logic | ミスパターンの頻度分析・弱点抽出 |
| `PracticeService` | Logic | 弱点に基づく練習単語の生成 |
| `StatisticsCalculator` | Logic | 期間別の統計計算（平均WPM・正答率） |
| `ResultFilter` | Logic | テスト結果の期間フィルタリング（今日・今週・今月） |
| `AnalyzerController` | Controller | RESTエンドポイントの定義、HTTPリクエスト/レスポンスの処理 |
| `AnalyzeService` | Service | APIリクエストの処理、Logic層の呼び出し、EntityとDTOの相互変換 |
| `TestResultEntity` | Entity | テスト結果のDB永続化、`TestResult`との相互変換 |
| `TestResultRepository` | Repository | JPAによるDBアクセス |

## API仕様
| メソッド | パス | 機能 |
|---|---|---|
| POST | `/api/analyze` | `MistakeAnalyzer`で正誤判定し、JSONで判定結果を返す |
| POST | `/api/results` | 判定結果をH2データベースに保存 |
| GET | `/api/results` | 過去の履歴を返す |
| GET | `/api/results/{id}` | 指定したidを持つ過去の履歴を返す |

## 品質保証

JUnit 5 による単体テストを整備し、リファクタリングの安全網を構築している。

| テストクラス | 主なテスト内容 |
|---|---|
| `MistakeAnalyzerTest` | 4種のミス検出、複合ミス、空文字、重複文字 |
| `TypingAnalyzerTest` | WPM・正答率の計算検証、null/空入力の例外処理 |
| `WeaknessAnalyzerTest` | 頻出ミスの抽出、ソート・件数制限、null/空入力 |
| `PracticeServiceTest` | 弱点単語の生成、サイズ制限、全問正解時の挙動 |
| `StatisticsCalculatorTest` | 平均計算、丸め処理、境界値 |
| `ResultFilterTest` | 日付境界値、うるう年、年末年始をまたぐ週 |

### テスト手法
- **パラメタライズドテスト**: 同一ロジックに対する複数パターンの効率的な検証
- **境界値テスト**: 日付の `0:00:00.000` / `23:59:59.999` など厳密な境界条件
- **Mockito**: 外部依存（`WordManager`）のモック化による単体テストの独立性確保

## 設計判断の記録

技術選定やアーキテクチャの意思決定は [ADR.md](ADR.md) に記録している。

## 今後の計画

- **Spring Boot REST API への移行**: 2026/03/21 完了
- **タイピングテスト・弱点分析・練習機能の追加**: 既存の`TestService`,`WeaknessAnalyzer`,`ResultFilter`,`PracticeService`等の既存クラスを活用し、タイピングテストの実施・弱点克服・練習機能をREST APIとして公開する
