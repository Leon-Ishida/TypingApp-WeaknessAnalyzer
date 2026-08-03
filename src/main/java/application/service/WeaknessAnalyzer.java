package application.service;

import application.model.MistakeDetail;
import application.model.MistakeType;
import application.model.TestResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 各ミスの種類ごとに起こりやすいミスを調べるクラス
 */

public class WeaknessAnalyzer {
    private final List<MistakeDetail> allMistakes;
    public record SubstitutionPair(Character expected, Character actual) {}
    public record TranspositionPair(Character char1, Character char2) {
        public TranspositionPair(Character char1, Character char2) {
            if (char1 > char2) {
                this.char1 = char1;
                this.char2 = char2;
            } else {
                this.char1 = char2;
                this.char2 = char1;
            }
        }
    }
    public record InsertionPair(Character beforeChar, Character afterChar, Character insertionChar) {}

    /**
     * 指定された期間で起きたミスをすべてリストにまとめる
     * @param targetResult ユーザーが指定した期間のテスト結果
     */
    public WeaknessAnalyzer(List<TestResult> targetResult) {
        if (targetResult == null || targetResult.isEmpty()) {
            this.allMistakes = Collections.emptyList();
            return;
        }
        this.allMistakes = targetResult.stream()
            .flatMap(results -> results.results().values().stream())
            .flatMap(wordResult -> wordResult.mistakes().stream())
            .toList();
    }

    /**
     * 置換ミスにおいて頻出のミスを頻度順に格納したMapを作るメソッド
     * @param limit 頻出のミスの何番目までを取るか決める引数
     * @return 置換ミスのペアと発生回数を格納したMap
     */
    public Map<SubstitutionPair, Long> findTopSubstitutionMistakes(int limit) {
        return findTopMistakes(MistakeType.SUBSTITUTION, mistake -> new SubstitutionPair(mistake.expected(), mistake.actual()), limit);
    }

    /**
     * 交換ミスにおいて頻出のミスを頻度順に格納したMapを作るメソッド
     * @param limit 頻出のミスの何番目までを取るか決める引数
     * @return 交換ミスのペアと発生回数を格納したMap
     */
    public Map<TranspositionPair, Long> findTopTranspositionMistakes(int limit) {
        return findTopMistakes(MistakeType.TRANSPOSITION, mistake -> new TranspositionPair(mistake.expected(), mistake.actual()), limit);
    }

    /**
     * 削除ミスにおいて頻出のミスを頻度順に格納したMapを作るメソッド
     * @param limit 頻出のミスの何番目までを取るか決める引数
     * @return 削除した文字と発生回数を格納したMap
     */
    public Map<Character, Long> findTopDeletionMistakes(int limit) {
        return findTopMistakes(MistakeType.DELETION, e -> e.expected(), limit);
    }

    /**
     * 挿入ミスにおいて頻出のミスを頻度順に格納したMapを作るメソッド
     * @param limit 頻出のミスの何番目までを取るか決める引数
     * @return 挿入した前後の文字のペアと発生回数を格納したMap
     */
    public Map<InsertionPair, Long> findTopInsertionMistakes(int limit) {
        return findTopMistakes(MistakeType.INSERTION, mistake -> new InsertionPair(mistake.expected(), mistake.actual(), mistake.insertion()), limit);
    }

    /**
     * 各ミスの種類に対して起こりやすいミスの上から何番目まで調べるメソッド
     * @param <T> ミスの種類に対応した起こりやすいミスを格納する型
     * @param type ミスの種類を指定する引数
     * @param keyMapper MistakeDetailから起こりやすいミスを格納する型に変換する関数
     * @param limit 起こりやすいミスの第番目までか決める引数
     * @return 起こりやすいミスを順番に格納したMap
     */
    private <T> Map<T, Long> findTopMistakes(MistakeType type, Function<MistakeDetail, T> keyMapper, int limit) {
        Map<T, Long> counts = this.allMistakes.stream()
            .filter(mistake -> mistake.mistakeType() == type)
            .collect(Collectors.groupingBy(keyMapper, Collectors.counting()));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<T, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue(),
                (e1, e2) -> e1,
                LinkedHashMap::new));
    }
}
