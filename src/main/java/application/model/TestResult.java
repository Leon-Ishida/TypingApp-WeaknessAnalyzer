package application.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * 1回のタイピングテストの結果を格納するデータクラス
 * @param timestamp プレイ終了時の時刻
 * @param results お題の単語とユーザーの解答のペアのマップ
 * @param wpm 計算したネットwpm
 * @param accuracy 計算した正答率
 */

public record TestResult(
    LocalDateTime timestamp,
    LinkedHashMap<String, WordResult> results,
    double wpm,
    double accuracy
) {}
