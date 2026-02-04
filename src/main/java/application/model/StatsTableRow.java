package application.model;

/**
 * 結果画面のミスの統計タブにある、期間ごとの平均WPMと平均正答率を示すテーブル表示用データクラス
 */

public record StatsTableRow(
    String period,
    double stats
) {}
