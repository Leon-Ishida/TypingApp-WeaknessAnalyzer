package application.service;

import application.model.TestResult;
import application.repository.TestResultRepository;
import application.security.CustomUserDetails;
import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;
import application.dto.PracticeGenerateRequest;
import application.entity.TestResultEntity;
import application.model.AppConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 練習用の単語リストを作るクラス
 */

@Service
public class PracticeService {
    private final WordManager wordManager;
    private final TestResultRepository repository;
    //各ミスの種類ごとに起こりやすいミスの第何位まで取るか決める定数
    private static final int WEAKNESS_ANALYSIS_LIMIT = 3;
    //各ミスの種類ごとSetに格納する単語数を決める定数
    private static final int ADD_LIMIT_FOR_SET = 3;

    //wordManagerを設定する
    public PracticeService(WordManager wordManager, TestResultRepository repository) {
        this.wordManager = wordManager;
        this.repository = repository;
    }

    public List<String> generatePracticeWords(PracticeGenerateRequest request, Authentication authentication) {
        List<TestResultEntity> selectedRecords;
        String userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();

        if (request.startDate() != null) {
        // 選択した期間の記録のみを抽出する
            LocalDateTime startDateTime = request.startDate().atStartOfDay();
            LocalDateTime lastDateTime = request.lastDate().plusDays(1).atStartOfDay();
            selectedRecords = repository.findByUserIdAndTimestampThanGreaterThanEqualAndTimestampLessThanOrderByTimestamp(userId, startDateTime, lastDateTime);
        } else {
            // 指定がない場合全期間の記録を抽出する
            selectedRecords = repository.findByUserIdOrderByTimestamp(userId);
        }
        
        List<TestResult> testResults = new ArrayList<>();
        for (TestResultEntity record : selectedRecords) {
            testResults.add(record.toRecord());
        }

        return switch (request.mode()) {
            case WEAKNESS -> generateWeaknessWords(testResults); // 指定した期間の中で各ミスの種類ごとに起こりやすいミスを含んだ単語リストを返す
            case FREQUENT -> generateFrequentWords(testResults); // 指定した期間の中でミスの頻度が多い単語リスト返す
        };
    }

    /**
     * 各ミスの種類ごとに起こりやすいミスを含んだ単語リストを作るメソッド
     * @param targetResult 指定した期間のテスト結果が格納されたリスト
     * @return 各ミスの種類ごとに起こりやすいミスを含んだ単語リスト
     */
    private List<String> generateWeaknessWords(List<TestResult> targetResult) {
        WeaknessAnalyzer weaknessAnalyzer = new WeaknessAnalyzer(targetResult);
        Set<String> resultSet = new HashSet<>();

        //置換ミスのミスの傾向を調べ、それに対応した単語を単語リストのSetに格納する
        Map<SubstitutionPair, Long> topSub = weaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT);
        if (!topSub.isEmpty()) {
            for (SubstitutionPair subPair : topSub.keySet()) {
                addWordsContainsChars(resultSet, ADD_LIMIT_FOR_SET, subPair.expected(), subPair.actual());
            }
        }

        //交換ミスのミスの傾向を調べ、それに対応した単語を単語リストのSetに格納する
        Map<TranspositionPair, Long> topTrans = weaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT);
        if (!topTrans.isEmpty()) {
            for (TranspositionPair transPair : topTrans.keySet()) {
                addWordsForTranspositionMistake(resultSet, ADD_LIMIT_FOR_SET, transPair);
            }
        }

        //削除ミスのミスの傾向を調べ、それに対応した単語を単語リストのSetに格納する
        Map<Character, Long> topDel = weaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT);
        if (!topDel.isEmpty()) {
            for (Character delCh : topDel.keySet()) {
                addWordsContainsChars(resultSet, ADD_LIMIT_FOR_SET, delCh);
            }
        }

        //挿入ミスのミスの傾向を調べ、それに対応した単語を単語リストのSetに格納する
        Map<InsertionPair, Long> topIns = weaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT);
        if (!topIns.isEmpty()) {
            for (InsertionPair insPair : topIns.keySet()) {
                addWordsForInsertionMistake(resultSet, ADD_LIMIT_FOR_SET, insPair);
            }
        }

        //単語数が問題数に満たない場合満たすようにランダムに追加する
        while (resultSet.size() < AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS) {
            resultSet.add(wordManager.getRandomWord());
        }
        //SetをListにして順番をシャッフルする
        List<String> wordList = new ArrayList<>(resultSet);
        Collections.shuffle(wordList);

        //単語数が問題数より多い場合、単語数を減らす
        if (wordList.size() > AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS) {
            return wordList.subList(0, AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS);
        }

        return wordList;
    }

    /**
     * 挿入ミスの傾向に対応した単語を探し、単語リストに格納するメソッド
     * 先頭に挿入した場合は正解の先頭の文字で始まる単語、末尾に挿入した場合は正解の末尾の文字で終わる単語、
     * 途中に挿入した場合はその前後の文字が続いて出てくる単語を探す
     * @param resultSet 見つけた単語を格納するSet
     * @param limit Setに格納する単語数(多めにするために2倍して使用)
     * @param pair 挿入した文字の前後の文字のペア
     */
    private void addWordsForInsertionMistake(Set<String> resultSet, int limit, InsertionPair pair) {
        Character beforeChar = pair.beforeChar();
        Character afterChar = pair.afterChar();
        Predicate<String> filter;
        if (beforeChar == null && afterChar != null) {
            //先頭に挿入した場合
            String prefix = String.valueOf(afterChar);
            filter = word -> word.startsWith(prefix);
        } else if (beforeChar != null && afterChar == null) {
            //末尾に挿入した場合
            String prefix = String.valueOf(beforeChar);
            filter = word -> word.endsWith(prefix);
        } else if (beforeChar != null && afterChar != null) {
            //途中に挿入した場合
            String sequence = String.valueOf(beforeChar) + String.valueOf(afterChar);
            filter = word -> word.contains(sequence);
        } else {
            //ガード節
            filter = word -> false;
        }

        addWordsWithFilter(resultSet, limit, filter);
    }

    /**
     * 交換ミスの傾向に対応した単語を探し、単語リストに格納するメソッド
     * @param resultSet 見つけた単語を格納するSet
     * @param limit Setに格納する単語数(多めにするために2倍して使用)
     * @param pair 交換が起こった2文字のペア
     */
    private void addWordsForTranspositionMistake(Set<String> resultSet, int limit, TranspositionPair pair) {
        String sequence1 = String.valueOf(pair.char1()) + String.valueOf(pair.char2());
        String sequence2 = String.valueOf(pair.char2()) + String.valueOf(pair.char1());
        Predicate<String> filter = word -> word.contains(sequence1) || word.contains(sequence2);

        addWordsWithFilter(resultSet, limit, filter);
    }

    /**
     * 置換ミスと削除ミス、それぞれの傾向に対応した単語を探し、単語リストに格納するメソッド
     * @param resultSet 見つけた単語を格納するSet
     * @param limit Setに格納する単語数(多めにするために2倍して使用)
     * @param chars 置換ミスか削除ミスが起きた文字
     */
    private void addWordsContainsChars(Set<String> resultSet, int limit, char... chars) {
        Predicate<String> filter = word -> {
            for (char c : chars) {
                if (word.indexOf(c) == -1) {
                    return false;
                }
            }
            return true;
        };

        addWordsWithFilter(resultSet, limit, filter);
    }

    /**
     * 各ミスの傾向に沿ったフィルターを使い単語を探して単語リストに格納するメソッド
     * @param resultSet 探した単語を格納するSet
     * @param limit Setに格納する単語数
     * @param filter 各ミスの傾向から適切な単語を決定する条件
     */
    private void addWordsWithFilter(Set<String> resultSet, int limit, Predicate<String> filter) {
        List<String> foundWords = this.wordManager.getWords().stream()
            .filter(filter.and(word -> !resultSet.contains(word)))
            .collect(Collectors.toList());
        Collections.shuffle(foundWords);
        foundWords.stream()
            .limit(limit)
            .forEach(resultSet::add);
    }

    /**
     * ミスの頻度が多い単語リストを作るメソッド
     * @param targetResult 指定した期間のテスト結果が格納されたリスト
     * @return 探した単語を格納したSet
     */
    private List<String> generateFrequentWords(List<TestResult> targetResult) {
        Set<String> resultSet = new HashSet<>();

        //選択した期間にミスがない場合ランダムな10個の単語を入れる
        if (targetResult != null && !targetResult.isEmpty()) {
            //テスト結果に保存されている各単語ごとにミスの回数をカウントしていく
            resultSet = targetResult.stream()
                .flatMap(testResult -> testResult.results().entrySet().stream())
                .filter(entry -> !entry.getValue().isCorrect())
                .map(e -> e.getKey())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                //{間違えた単語, 間違えた回数}というMapを作る
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS)
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
        }
        //テスト結果がnullまたは空の時、ミスした単語が10個に届かないときランダムな単語を加える
        while (resultSet.size() < AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS) {
            resultSet.add(wordManager.getRandomWord());
        }

        List<String> wordList = new ArrayList<>(resultSet);

        if (wordList.size() > AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS) {
            return wordList.subList(0, AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS);
        }

        return wordList;
    }
}
