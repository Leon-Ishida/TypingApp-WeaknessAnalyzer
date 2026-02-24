package application.typingtest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import application.model.MistakeDetail;
import application.model.MistakeType;
import application.model.TestResult;
import application.model.WordResult;
import application.util.MistakeAnalyzer;
import java.util.List;

/**
 * ミスの特定と今回のテストにおけるwpmと正答率を計算するクラス
 */

public class TypingAnalyzer {
    /**
     * テスト結果と所要時間を受け取り、ミスの特定と統計を管理するメソッド
     * @param rawResult テスト結果のみが入ったMap
     * @param usedTimeMillis 所要時間(ミリ秒)
     * @return テスト日時、ミスの種類及び統計を持った完全なテスト結果を含んだデータクラス
     */
    public TestResult analyze(LinkedHashMap<String, String> rawResult, long usedTimeMillis) {
        if (rawResult == null || rawResult.isEmpty()) {
            throw new IllegalArgumentException("テスト結果が空です");
        }
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();
        int totalChars = 0;
        int correctCount = 0;

        for (String word : rawResult.keySet()) {
            String answer = rawResult.get(word);
            WordResult eachWordResult = analyzeMistake(word, answer);
            results.put(word, eachWordResult);

            //正解したときのみ正解数と入力文字数を増やす
            if (eachWordResult.isCorrect()) {
                correctCount++;
                totalChars += word.length();
            }
        }

        double wpm = calculateWpm(usedTimeMillis, totalChars);
        double accuracy = calculateAccuracy(correctCount, rawResult.size());
        return new TestResult(LocalDateTime.now(), results, wpm, accuracy);
    }

    /**
     * 各単語に対しどのミスがあるか特定するメソッド
     * @param word 正解の単語
     * @param answer ユーザーの入力結果
     * @return ユーザーの入力とこの単語内で見られるすべてのミスを含んだリストを格納したデータクラス
     */
    public WordResult analyzeMistake(String word, String answer) {
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes(word, answer);
        return new WordResult(answer, mistakes);
    }

    /**
     * 5文字を1単語としてネットwpmを計算するメソッド
     * @param usedTimeMillis 所要時間(ミリ秒)
     * @param totalChars 正解した単語に限定したときの入力文字数
     * @return double型のwpm
     */
    public double calculateWpm(long usedTimeMillis, int totalChars) {
        if (usedTimeMillis <= 0) {
            return 0.0;
        }
        //所要時間の単位を分に直す
        double usedTimeMinutes = (double) usedTimeMillis / 1000.0 / 60.0;

        //5文字を1単語として単語数を計算する
        double wordCount = (double) totalChars / 5.0;
        return wordCount / usedTimeMinutes;
    }

    /**
     * 単語単位で正答率を計算するメソッド
     * @param correctCount 正解した単語数
     * @param wordCount 問題数
     * @return double型の正答率
     */
    public double calculateAccuracy(int correctCount, int wordCount) {
        return (double) correctCount / (double) wordCount;
    }
}
