package application.result;

import java.util.List;

import application.model.DisplayStatistics;
import application.model.FilteredResult;
import application.model.StatisticsResult;

/**
 * 各期間ごとに計算した平均wpmと平均正答率を格納するクラス
 */

public class StatisticsService {
    //インスタンス化禁止
    private StatisticsService() {}

    /**
     * 各期間ごとに計算し、計算結果を格納したデータクラスをDisplayStatisticsオブジェクトに格納するメソッド
     * @param filteredResult 各期間ごとに分けられた結果リストを格納したFilteredResultオブジェクト
     * @return 計算結果を格納したStatisticsResultオブジェクトを格納したDisplayStatisticsオブジェクトを返す
     */
    public static DisplayStatistics createDisplayStatistics(FilteredResult filteredResult) {
        StatisticsResult lastStats = StatisticsCalculator.calculate(List.of(filteredResult.lastResult()));
        StatisticsResult todayStats = StatisticsCalculator.calculate(filteredResult.todayResults());
        StatisticsResult thisWeekStats = StatisticsCalculator.calculate(filteredResult.thisWeekResults());
        StatisticsResult thisMonthResult = StatisticsCalculator.calculate(filteredResult.thisMonthResults());
        StatisticsResult allTimeResult = StatisticsCalculator.calculate(filteredResult.allResults());
        StatisticsResult recent10Result = StatisticsCalculator.calculate(filteredResult.recent10Results());

        return new DisplayStatistics(
            lastStats,
            todayStats,
            thisWeekStats,
            thisMonthResult,
            allTimeResult,
            recent10Result
        );
    }
}
