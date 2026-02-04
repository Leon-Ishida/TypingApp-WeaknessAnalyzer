package application.model;

/**
 * 各期間の統計データである平均wpmと平均正答率を格納するデータクラス
 * @param last 前回の統計データ
 * @param today 今日の統計データ
 * @param thisWeek 今週の統計データ
 * @param thisMonth 今月の統計データ
 * @param allTime 全期間の統計データ
 * @param recent10 直近10回の統計データ
 */

public record DisplayStatistics(
    StatisticsResult last,
    StatisticsResult today,
    StatisticsResult thisWeek,
    StatisticsResult thisMonth,
    StatisticsResult allTime,
    StatisticsResult recent10
) {}
