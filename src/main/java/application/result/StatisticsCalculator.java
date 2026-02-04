package application.result;

import java.util.List;
import application.model.StatisticsResult;
import application.model.TestResult;

/**
 * 結果リストを取得して平均wpmと平均正答率を計算するクラス
 */

public class StatisticsCalculator {
    //インスタンス化禁止
    private StatisticsCalculator() {}

    /**
     * 小数第3位までに丸めるために使用する定数
     * (例: 12.3456 * {@value #ROUNDING_MULTIPLIER} = 12345.6 → round(12345.6) = 12346 → 12346 / 1000.0 = 12.346)
     */
    private static double ROUNDING_MULTIPLIER = 1000.0;

    /**
     * 平均wpmと平均正答率を計算するメソッド
     * @param results 結果のリスト
     * @return 小数第3位に丸めた平均wpmと平均正答率を格納したStatisticsResultオブジェクトを返す
     * もし結果のリストがnullまたは空の場合平均wpmと平均正答率をどちらも0.0にしたStatisticsResultオブジェクトを返す
     */
    public static StatisticsResult calculate(List<TestResult> results) {
        //ガード節
        if (results == null || results.isEmpty()) {
            return new StatisticsResult(0.0, 0.0);
        }

        double wpmSum = 0.0;
        double AccuracySum = 0.0;

        for (TestResult result : results) {
            wpmSum += result.wpm();
            AccuracySum += result.accuracy();
        }

        double averageWpm = Math.round(wpmSum / results.size() * StatisticsCalculator.ROUNDING_MULTIPLIER) / StatisticsCalculator.ROUNDING_MULTIPLIER;
        double averageAccuracy = Math.round(AccuracySum / results.size() * StatisticsCalculator.ROUNDING_MULTIPLIER) / StatisticsCalculator.ROUNDING_MULTIPLIER;

        return new StatisticsResult(averageWpm, averageAccuracy);
    }
}
