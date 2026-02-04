package application.model;

/**
 * 結果画面のミスの統計タブにある、主なミスの傾向における置換、交換、挿入ミスのテーブル表示用データクラス
 */

public record WeaknessPairRow(
    String mistakePair,
    long count
) {}
