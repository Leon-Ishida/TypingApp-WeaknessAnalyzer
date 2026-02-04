package application.model;

/**
 * テスト1回における平均wpmと平均正答率を格納するデータクラス
 * @param averageWpm テスト結果から計算された平均wpm
 * @param averageAccuracy テスト結果から計算された平均正答率
 */

public record StatisticsResult(
    double averageWpm,
    double averageAccuracy
) {}
