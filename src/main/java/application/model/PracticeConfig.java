package application.model;

/**
 * 練習の期間モードを保持するデータクラス
 */

public record PracticeConfig(
    Period period, //分析対象とする期間
    PracticeMode mode //練習のモード
) {}
