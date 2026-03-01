package application.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import application.model.MistakeDetail;
import application.model.MistakeType;
import application.model.TestResult;
import application.model.WordResult;
import application.service.WeaknessAnalyzer.*;

public class WeaknessAnalyzerTest {
    
    //置換ミスのテスト
    static Stream<Arguments> provideSubstitutionTestCases() {
        return Stream.of(
            //置換ミスが1回の場合
            Arguments.of(
                "置換ミスが1回",
                List.of(createMockResult("word", new MistakeDetail(MistakeType.SUBSTITUTION, 'x', 'y', '\0', 0))),
                new SubstitutionPair('x', 'y'),
                1L
            ),

            //同じ置換ミスが複数ある場合
            Arguments.of(
                "同じ置換ミスが2回",
                List.of(
                    createMockResult("word1", new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 0)),
                    createMockResult("word2", new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 1))    
                ),
                new SubstitutionPair('a', 'b'),
                2L
            ),

            //ミスがない場合
            Arguments.of(
                "ミスなし",
                List.of(createMockResult("correct", new MistakeDetail[0])),
                null,
                0L
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSubstitutionTestCases")
    void testSubstitutionAnalyzer(String testName, List<TestResult> targetResult, SubstitutionPair expectedPair, Long expectedCount) {
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(targetResult);
        Map<SubstitutionPair, Long> result = analyzer.findTopSubstitutionMistakes(1);
        
        if (expectedPair == null) {
            assertTrue(result.isEmpty(), "空のMapが返される必要があります");
        } else {
            assertFalse(result.isEmpty(), "空でないMapが返される必要があります");
            assertTrue(result.containsKey(expectedPair), "期待されるべきペアが含まれていません");
            assertEquals(expectedCount, result.get(expectedPair), "カウント数が一致しません");
        }
    }

    //交換ミスのテスト
    static Stream<Arguments> provideTranspositionTestCases() {
        return Stream.of(
            //交換ミスが1回の場合
            Arguments.of(
                "交換ミスが1回",
                List.of(createMockResult("ab", new MistakeDetail(MistakeType.TRANSPOSITION, 'a', 'b', '\0', 0))),
                new TranspositionPair('a', 'b'),
                1L
            ),

            //順序の正規化を確認
            Arguments.of(
                "順序の正規化",
                List.of(
                    createMockResult("ab", new MistakeDetail(MistakeType.TRANSPOSITION, 'a', 'b', '\0', 0)),
                    createMockResult("ba", new MistakeDetail(MistakeType.TRANSPOSITION, 'b', 'a', '\0', 1))
                ),
                new TranspositionPair('a', 'b'),
                2L
            ),

            //ミスがない場合
            Arguments.of(
                "ミスなし",
                List.of(createMockResult("correct", new MistakeDetail[0])),
                null,
                0L
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTranspositionTestCases")
    void testTranspositionAnalyzer(String testName, List<TestResult> targetResult, TranspositionPair expectedPair, Long expectedCount) {
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(targetResult);
        Map<TranspositionPair, Long> result = analyzer.findTopTranspositionMistakes(1);

        if (expectedPair == null) {
            assertTrue(result.isEmpty(), "空のMapが返される必要があります");
        } else {
            assertFalse(result.isEmpty(), "空でないMapが返される必要があります");
            assertTrue(result.containsKey(expectedPair), "期待されるべきペアが含まれていません");
            assertEquals(expectedCount, result.get(expectedPair), "カウント数が一致しません");
        }    
    }

    //削除ミスのテスト
    static Stream<Arguments> provideDeletionTestCases() {
        return Stream.of(
            //削除ミスが1回の場合
            Arguments.of(
                "削除ミス1回",
                List.of(createMockResult("test", new MistakeDetail(MistakeType.DELETION, 'x', '\0', '\0', 0))),
                'x',
                1L
            ),

            //削除ミスが複数ある場合
            Arguments.of(
                "同じ削除ミス2回",
                List.of(
                    createMockResult("test", new MistakeDetail(MistakeType.DELETION, 'x', '\0', '\0', 0)),
                    createMockResult("test", new MistakeDetail(MistakeType.DELETION, 'x', '\0', '\0', 1))
                ),
                'x',
                2L
            ),

            //ミスがない場合
            Arguments.of(
                "ミスなし",
                List.of(createMockResult("correct", new MistakeDetail[0])),
                null,
                0L
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideDeletionTestCases")
    void testDeletionAnalyzer(String testName, List<TestResult> targetResult, Character expectedCharacter, Long expectedCount) {
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(targetResult);
        Map<Character, Long> result = analyzer.findTopDeletionMistakes(1);

        if (expectedCharacter == null) {
            assertTrue(result.isEmpty(), "空のMapが返される必要があります");
        } else {
            assertFalse(result.isEmpty(), "空でないMapが返される必要があります");
            assertTrue(result.containsKey(expectedCharacter), "期待されるべき文字が含まれていません");
            assertEquals(expectedCount, result.get(expectedCharacter), "カウント数が一致しません");
        }
    }

    //挿入ミスのテスト
    static Stream<Arguments> provideInsertionTestCases() {
        return Stream.of(
            //挿入ミスが1回の場合
            Arguments.of(
                "挿入ミス1回",
                List.of(createMockResult("test", new MistakeDetail(MistakeType.INSERTION, 'a', 'b', 'x', 0))),
                new InsertionPair('a', 'b', 'x'),
                1L
            ),

            //挿入ミスが複数の場合
            Arguments.of(
                "同じ挿入ミス2回",
                List.of(
                    createMockResult("test", new MistakeDetail(MistakeType.INSERTION, 'a', 'b', 'x', 0)),
                    createMockResult("test", new MistakeDetail(MistakeType.INSERTION, 'a', 'b', 'x', 1))
                ),
                new InsertionPair('a', 'b', 'x'),
                2L
            ),

            //ミスがない場合
            Arguments.of(
                "ミスなし",
                List.of(createMockResult("correct", new MistakeDetail[0])),
                null,
                0L
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsertionTestCases")
    void testInsertionAnalyzer(String testName, List<TestResult> targetResult, InsertionPair expectedPair, Long expectedCount) {
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(targetResult);
        Map<InsertionPair, Long> result = analyzer.findTopInsertionMistakes(1);

        if (expectedPair == null) {
            assertTrue(result.isEmpty(), "空のMapが返される必要があります");
        } else {
            assertFalse(result.isEmpty(), "空でないMapが返される必要があります");
            assertTrue(result.containsKey(expectedPair), "期待されるべきペアが含まれていません");
            assertEquals(expectedCount, result.get(expectedPair), "カウント数が一致しません");
        }
    }

    /**
     * テスト用のTestResultオブジェクトを生成するメソッド
     * @param word お題の単語
     * @param mistakeDetails 可変長である発生したミスの詳細
     * @return 生成されたTestResult
     */
    private static TestResult createMockResult(String word, MistakeDetail... mistakeDetails) {
        WordResult wordResult = new WordResult("dummy", List.of(mistakeDetails));
        LinkedHashMap<String, WordResult> resultMap = new LinkedHashMap<>();
        resultMap.put(word, wordResult);
        return new TestResult(LocalDateTime.now(), resultMap, 0.0, 0.0);
    }

    // ソート機能とリミット機能のテスト 置換ミスを用いる
    @Test
    @DisplayName("ソート機能とリミット機能検証")
    void testSortingAndLimit() {
        // データ準備: 
        // 'a'->'b' (3回), 'c'->'d' (2回), 'e'->'f' (1回)
        List<TestResult> results = List.of(
            createMockResult("word1", 
                new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 0),
                new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 1),
                new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 2),
                new MistakeDetail(MistakeType.SUBSTITUTION, 'c', 'd', '\0', 3),
                new MistakeDetail(MistakeType.SUBSTITUTION, 'c', 'd', '\0', 4),
                new MistakeDetail(MistakeType.SUBSTITUTION, 'e', 'f', '\0', 5)
            )
        );

        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(results);

        // 上位2件を取得
        Map<SubstitutionPair, Long> result = analyzer.findTopSubstitutionMistakes(2);

        // 検証1: 数が2つであること
        assertEquals(2, result.size(), "結果は2件であるべきです");

        // 検証2: 上位2つが含まれていること (a->b, c->d)
        SubstitutionPair pair1 = new SubstitutionPair('a', 'b');
        SubstitutionPair pair2 = new SubstitutionPair('c', 'd');
        SubstitutionPair pair3 = new SubstitutionPair('e', 'f');

        assertTrue(result.containsKey(pair1), "1位のミスが含まれていません");
        assertTrue(result.containsKey(pair2), "2位のミスが含まれていません");
        assertFalse(result.containsKey(pair3), "3位以下のミスは含まれてはいけません");

        // 検証3: カウント数が正しいこと
        assertEquals(3L, result.get(pair1));
        assertEquals(2L, result.get(pair2));
        
        // 検証4: 順序の確認（Mapのイテレータ順序が挿入順=ランキング順になっているか）
        // WeaknessAnalyzerの実装で LinkedHashMap を使って順序を保持しているため、このテストが成立する
        var iterator = result.entrySet().iterator();
        assertEquals(pair1, iterator.next().getKey(), "1番目は最も多いミスであるべき");
        assertEquals(pair2, iterator.next().getKey(), "2番目は2番目に多いミスであるべき");
    }

    // 異なるミスタイプが混在する場合のフィルタリングテスト
    @Test
    @DisplayName("異なるミスが混在する場合のフィルタリング検証")
    void testMixedMistakeTypes() {
        // 置換(Substitution)と削除(Deletion)を混ぜる
        List<TestResult> results = List.of(
            createMockResult("word1", 
                new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 0),
                new MistakeDetail(MistakeType.DELETION, 'c', '\0', '\0', 1)
            )
        );

        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(results);

        // 置換だけを取得
        Map<SubstitutionPair, Long> subResult = analyzer.findTopSubstitutionMistakes(10);
        assertEquals(1, subResult.size(), "置換ミスだけが抽出されるべき");
        assertTrue(subResult.containsKey(new SubstitutionPair('a', 'b')));

        // 削除だけを取得
        Map<Character, Long> delResult = analyzer.findTopDeletionMistakes(10);
        assertEquals(1, delResult.size(), "削除ミスだけが抽出されるべき");
        assertTrue(delResult.containsKey('c'));
    }

    @Test
    @DisplayName("nullまたは空リスト入力時の検証")
    void testNullOrEmptyInput() {
        WeaknessAnalyzer nullAnalyzer = new WeaknessAnalyzer(null);
        assertTrue(nullAnalyzer.findTopSubstitutionMistakes(3).isEmpty(), "null入力時は空リストが返されるべき");

        WeaknessAnalyzer emptyAnalyzer = new WeaknessAnalyzer(List.of());
        assertTrue(emptyAnalyzer.findTopSubstitutionMistakes(3).isEmpty(), "空リスト入力時は空リストが返されるべき");
    }

    @Test
    @DisplayName("同じミスの種類がlimitより少ない場合の検証")
    void testLimitExceedsActualMistakeCount() {
        List<TestResult> targetResult = List.of(
            createMockResult("word1", new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', '\0', 0)),
            createMockResult("word2", new MistakeDetail(MistakeType.SUBSTITUTION, 'x', 'y', '\0', 1))
        );
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(targetResult);
        Map<SubstitutionPair, Long> result = analyzer.findTopSubstitutionMistakes(5);
        
        assertEquals(2, result.size(), "limitがミスの件数より多い場合、すべてのミスが返されるべき");
    }

}
