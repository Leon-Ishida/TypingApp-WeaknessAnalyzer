package application.model;

/**
 * 結果画面のミスの統計タブにある、主なミスの傾向における削除ミスのテーブル表示用データクラス
 */

public record WeaknessCharRow(
    char character,
    long count
) {}
