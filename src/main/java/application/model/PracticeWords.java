package application.model;

import java.util.List;

/**
 * 練習用の単語リストを格納するデータクラス
 * @param weaknessWords 各ミスの種類ごとに起こりやすいミスを含んだ単語リスト
 * @param frequentMistakeWords 頻繫に間違える単語リスト
 */

public record PracticeWords(
    List<String> weaknessWords,
    List<String> frequentMistakeWords
) {}
