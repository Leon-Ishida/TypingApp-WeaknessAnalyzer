package application.model;

/**
 * 結果画面のミスの統計タブにある、ミスの種類ごとの回数のテーブル表示用データクラス
 */

public record MistakeStatsRow(
    String mistakeType,
    int count
) {}
