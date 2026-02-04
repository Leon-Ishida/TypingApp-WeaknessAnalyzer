package application.model;

/**
 * 結果画面の誤答リストタブにある、誤答リストのテーブル表示用データクラス
 */

public record MistakeDetailRow(
    String questionWord,
    String answerWord,
    int subCount,
    int transCount,
    int delCount,
    int insCount
) {}
