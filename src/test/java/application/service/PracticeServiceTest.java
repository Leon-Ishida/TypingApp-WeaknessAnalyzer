package application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import application.model.MistakeDetail;
import application.model.MistakeType;
import application.model.PracticeWords;
import application.model.TestResult;
import application.model.WordResult;

@ExtendWith(MockitoExtension.class)
public class PracticeServiceTest {
    @Mock
    private WordManager mockWordManager;

    @InjectMocks
    private PracticeService practiceService;

    private final List<String> mockDictUnderTenionary = List.of(
            "assert", "sample", "sat", "as", "assemble", "save", "apple", "sun", "book", "appart"
    );

    @BeforeEach
    void setup() {
        setupMockWordManager(mockDictUnderTenionary);
    }

    @Test
    @DisplayName("置換ミス: aとsがどちらも存在する単語を抽出されること")
    void weaknessWordsSubstitution() {
        List<String> weaknessWords = setupWeaknessWords(createSubstitutionMistake('a', 's'));
        List<String> removeDummy = removeDummy(weaknessWords);

        assertAll("弱点克服用練習単語の検証(置換ミス)",
            () -> assertEquals(3, removeDummy.size(), "置換ミス練習単語は3個であること"),
            () -> assertTrue(removeDummy.stream().allMatch(s -> s.contains("a") && s.contains("s")), "置換ミス練習単語にはaとsが含まれるべき"),
            () -> assertEquals(10, weaknessWords.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("交換ミス: aとsが順不同で連続する単語を抽出されること")
    void weaknessWordsTransposition() {
        List<String> weaknessWords = setupWeaknessWords(createTranspositionMistake('a', 's'));
        List<String> removeDummy = removeDummy(weaknessWords);

        assertAll("弱点克服用練習単語の検証(交換ミス)",
            () -> assertEquals(3, removeDummy.size(), "交換ミス練習単語は3個であること"),
            () -> assertTrue(removeDummy.stream().allMatch(s -> s.contains("as") || s.contains("sa")), "交換ミス練習単語にはaとsが順不同で連続して含まれるべき"),
            () -> assertEquals(10, weaknessWords.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("削除ミス: aが含まれる単語を抽出されること")
    void weaknessWordsDeletion() {
        List<String> weaknessWords = setupWeaknessWords(createDeletionMistake('a'));
        List<String> removeDummy = removeDummy(weaknessWords);

        assertAll("弱点克服用練習単語の検証(削除ミス)",
            () -> assertEquals(3, removeDummy.size(), "削除ミス練習単語は3個であること"),
            () -> assertTrue(removeDummy.stream().allMatch(s -> s.contains("a")), "削除ミス練習単語にはaが含まれるべき"),
            () -> assertEquals(10, weaknessWords.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("先頭での挿入ミス: 単語の先頭の文字が同じな単語を抽出されること")
    void weaknessWordInsertionFirst() {
        List<String> weaknessWordsFirst = setupWeaknessWords(createInsertionMistake('\0', 'a', 't'));
        List<String> removeDummyFirst = removeDummy(weaknessWordsFirst);

        assertAll("弱点克服用練習単語の検証(先頭での挿入ミス)",
            () -> assertEquals(3, removeDummyFirst.size(), "挿入ミス練習単語は3個であること"),
            () -> assertTrue(removeDummyFirst.stream().allMatch(s -> s.startsWith("a")), "挿入ミス練習単語はaから始まるべき"),
            () -> assertEquals(10, weaknessWordsFirst.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("文中での挿入ミス: 挿入した文字の前後が連続する単語を抽出されること")
    void weaknessWordInsertionMiddle() {
        List<String> weaknessWordsMiddle = setupWeaknessWords(createInsertionMistake('a', 's', 't'));
        List<String> removeDummyMiddle = removeDummy(weaknessWordsMiddle);

        assertAll("弱点克服用練習単語の検証(文中での挿入ミス)",
            () -> assertEquals(3, removeDummyMiddle.size(), "挿入ミス練習単語は3個であること"),
            () -> assertTrue(removeDummyMiddle.stream().allMatch(s -> s.contains("as")), "挿入ミス練習単語にasが含まれること"),
            () -> assertEquals(10, weaknessWordsMiddle.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("末尾での挿入ミス: 単語の末尾の文字が同じな単語を抽出されること")
    void weaknessWordInsertionLast() {
        List<String> weaknessWordsLast = setupWeaknessWords(createInsertionMistake('e', '\0', 't'));
        List<String> removeDummyLast = removeDummy(weaknessWordsLast);

        assertAll("弱点克服用練習単語の検証(末端での挿入ミス)",
            () -> assertEquals(3, removeDummyLast.size(), "挿入ミス練習単語は3個であること"),
            () -> assertTrue(removeDummyLast.stream().allMatch(s -> s.endsWith("e")), "挿入ミス練習単語の末尾がeであること"),
            () -> assertEquals(10, weaknessWordsLast.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("複合ミス: 置換と交換が混在する場合、両方の弱点単語が重複なく抽出されること")
    void weaknessWordComposite() {
        List<String> mockDictUnderTenionaryComposite = List.of(
            "sub_xy_1", "sub_yx_2",
            "trans_as_1", "trans_sa_2",
            "both_xy_sa",
            "trans_extra_as"
        );
        when(mockWordManager.getWords()).thenReturn(mockDictUnderTenionaryComposite);

        List<TestResult> compositeResults = new ArrayList<>();
        compositeResults.addAll(createSubstitutionMistake('x', 'y'));
        compositeResults.addAll(createTranspositionMistake('a', 's'));
        List<String> weaknessWords = setupWeaknessWords(compositeResults);
        List<String> removeDummy = removeDummy(weaknessWords);

        assertAll("弱点克服用練習単語の検証(置換と交換の混合ミス)",
            () -> assertEquals(6, removeDummy.size(), "練習単語の合計は6個であること"),
            () -> assertTrue(removeDummy.stream().anyMatch(s -> s.startsWith("sub")), "置換用単語が含まれていること"),
            () -> assertTrue(removeDummy.stream().anyMatch(s -> s.startsWith("trans")), "交換用単語が含まれていること"),
            () -> assertEquals(removeDummy.stream().distinct().count(), removeDummy.size(), "重複単語が含まれていないこと"),
            () -> assertEquals(10, weaknessWords.size(), "合計10個であること")
        );
    }

    @Test
    @DisplayName("弱点克服練習単語は10件でなければならない")
    void testWeaknessWordsSize() {
        //練習単語が10件に満たない場合
        List<String> mockDictUnderTen = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mockDictUnderTen.add("xy_" + String.valueOf(i));
        }
        when(mockWordManager.getWords()).thenReturn(mockDictUnderTen);

        List<TestResult> resultsUnderTen = createTranspositionMistake('x', 'y');
        List<String> weaknessWordsUnderTen = setupWeaknessWords(resultsUnderTen);
        List<String> removeDummyWordsUnderTen = removeDummy(weaknessWordsUnderTen);
        assertAll("弱点克服練習単語のサイズ検証(10件に満たない場合)",
            () -> assertEquals(3, removeDummyWordsUnderTen.size(), "弱点克服単語リストにダミーは7個であるべき"),
            () -> assertEquals(10, weaknessWordsUnderTen.size(), "弱点克服練習単語は合計10件であること")
        );

        //練習単語が10件より多い場合
        List<String> mockDictOverTen = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mockDictOverTen.add("sub_ab_" + String.valueOf(i));
            mockDictOverTen.add("trans_cd_" + String.valueOf(i));
            mockDictOverTen.add("del_ef_" + String.valueOf(i));
            mockDictOverTen.add("ins_gh_" + String.valueOf(i));
        }
        when(mockWordManager.getWords()).thenReturn(mockDictOverTen);

        List<TestResult> resultsOverTen = new ArrayList<>();
        resultsOverTen.addAll(createSubstitutionMistake('a', 'b'));
        resultsOverTen.addAll(createTranspositionMistake('c', 'd'));
        resultsOverTen.addAll(createDeletionMistake('e'));
        resultsOverTen.addAll(createInsertionMistake('g', 'h', 'x'));

        List<String> weaknessWordsOverTen = setupWeaknessWords(resultsOverTen);
        List<String> removeDummyWordsOverTen = removeDummy(weaknessWordsOverTen);
        assertAll("弱点克服練習単語のサイズ検証(10件より多い場合)",
            () -> assertEquals(10, removeDummyWordsOverTen.size(), "弱点克服単語リストにはダミーは含まれていてはならない"),
            () -> assertEquals(10, weaknessWordsOverTen.size(), "弱点克服単語は合計10件であること")
        );
    }

    @Test
    @DisplayName("頻出ミス単語は10件でなければならない")
    void testFrequentWordsSize() {
        //頻出ミスが10件に満たない場合
        List<TestResult> resultsUnderTen = createFrequentMistake(5);
        List<String> frequentWordsUnderTen = setupFrequentWords(resultsUnderTen);
        List<String> removeDummyWordsUnderTen = removeDummy(frequentWordsUnderTen);

        assertAll("頻出ミス単語のサイズ検証(10件に満たない場合)",
            () -> assertEquals(5, removeDummyWordsUnderTen.size(), "頻出ミス単語リストにダミーは5個であるべき"),
            () -> assertEquals(10, frequentWordsUnderTen.size(), "頻出ミス単語リストは10件であること")
        );

        //頻出ミスが10件より多い場合
        List<TestResult> resultsOverTen = createFrequentMistake(15);
        List<String> frequentWordsOverTen = setupFrequentWords(resultsOverTen);
        List<String> removeDummyWordsOverTen = removeDummy(frequentWordsOverTen);

        assertAll("頻出ミス単語のサイズ検証(10件より多い場合)",
            () -> assertEquals(10, removeDummyWordsOverTen.size(), "頻出ミス単語リストにダミーは含まれてはならない"),
            () -> assertEquals(10, frequentWordsOverTen.size(), "頻出ミス単語リストは10件であるべき")
        );
    }

    @Test
    @DisplayName("頻出ミス単語はミス回数上位10件が選ばれること")
    void testFrequentWordsSelection() {
        List<TestResult> results = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            results.addAll(createFrequentMistake(i + 1));
        }
        List<String> frequentWords = setupFrequentWords(results);

        List<String> expectedWords = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            expectedWords.add("freq_" + String.valueOf(i));
        }
        assertAll("頻出ミス単語の上位10件選出検証",
            () -> assertEquals(10, frequentWords.size(), "10件であること"),
            () -> assertTrue(frequentWords.containsAll(expectedWords), "ミス回数上位10件がすべて含まれること"),
            () -> assertFalse(frequentWords.contains("freq_10"), "ミス2回のfreq_10が含まれないこと"),
            () -> assertFalse(frequentWords.contains("freq_11"), "ミス1回のfreq_11が含まれないこと")
        );
    }

    @Test
    @DisplayName("入力がnullまたは空の場合、どちらの練習単語もランダムな単語が10個含まれること")
    void testNonOrNullData() {
        //入力がnullの時
        List<String> weaknessWordsOfNull = setupWeaknessWords(null);
        List<String> frequentWordsOfNull = setupFrequentWords(null);
        List<String> removeDummyWeaknessWordsOfNull = removeDummy(weaknessWordsOfNull);
        List<String> removeDummyFrequentWordsOfNull = removeDummy(frequentWordsOfNull);

        assertAll("null入力時の単語リスト検証",
            () -> assertEquals(10, weaknessWordsOfNull.size(), "弱点克服練習単語は10件であるべき"),
            () -> assertEquals(10, frequentWordsOfNull.size(), "頻出ミス単語は10件であるべき"),
            () -> assertEquals(0, removeDummyWeaknessWordsOfNull.size(), "弱点克服練習単語はすべてダミーであるべき"),
            () -> assertEquals(0, removeDummyFrequentWordsOfNull.size(), "頻出ミス単語はすべてダミーであるべき")
        );

        //入力が空の時
        List<String> weaknessWordsOfNon = setupWeaknessWords(Collections.emptyList());
        List<String> frequentWordsOfNon = setupFrequentWords(Collections.emptyList());
        List<String> removeDummyWeaknessWordsOfNon = removeDummy(weaknessWordsOfNon);
        List<String> removeDummyFrequentWordsOfNon = removeDummy(frequentWordsOfNon);

        assertAll("空リスト入力時の単語リスト検証",
            () -> assertEquals(10, weaknessWordsOfNon.size(), "弱点克服練習単語は10件であるべき"),
            () -> assertEquals(10, frequentWordsOfNon.size(), "頻出ミス単語は10件であるべき"),
            () -> assertEquals(0, removeDummyWeaknessWordsOfNon.size(), "弱点克服練習単語はすべてダミーであるべき"),
            () -> assertEquals(0, removeDummyFrequentWordsOfNon.size(), "頻出ミス単語はすべてダミーであるべき")
        );
    }

    private List<TestResult> createSubstitutionMistake(char a1, char a2) {
        WordResult testWordResult = new WordResult("testWord", List.of(new MistakeDetail(MistakeType.SUBSTITUTION, a1, a2, '\0', 0)));
        LinkedHashMap<String, WordResult> testResults = new LinkedHashMap<>();
        testResults.put("testWord", testWordResult);
        TestResult testResult = new TestResult(LocalDateTime.now(), testResults, 0.0,0.0);
        return List.of(testResult);
    }

    private List<TestResult> createTranspositionMistake(char a1, char a2) {
        WordResult testWordResult = new WordResult("testWord", List.of(new MistakeDetail(MistakeType.TRANSPOSITION, a1, a2, '\0', 0)));
        LinkedHashMap<String, WordResult> testResults = new LinkedHashMap<>();
        testResults.put("testWord", testWordResult);
        TestResult testResult = new TestResult(LocalDateTime.now(), testResults, 0.0, 0.0);
        return List.of(testResult);
    }

    private List<TestResult> createDeletionMistake(char a1) {
        WordResult testWordResult = new WordResult("testWord", List.of(new MistakeDetail(MistakeType.DELETION, a1, '\0', '\0', 0)));
        LinkedHashMap<String, WordResult> testResults = new LinkedHashMap<>();
        testResults.put("testWord", testWordResult);
        TestResult testResult = new TestResult(LocalDateTime.now(), testResults, 0.0, 0.0);
        return List.of(testResult);
    }

    private List<TestResult> createInsertionMistake(char beforeChar, char afterChar, char insertionChar) {
        WordResult testWordResult = new WordResult("testWord", List.of(new MistakeDetail(MistakeType.INSERTION, beforeChar, afterChar, insertionChar, 0)));
        LinkedHashMap<String, WordResult> testResults = new LinkedHashMap<>();
        testResults.put("testWord", testWordResult);
        TestResult testResult = new TestResult(LocalDateTime.now(), testResults, 0.0, 0.0);
        return List.of(testResult);
    }

    private void setupMockWordManager(List<String> dictionary) {
        lenient().when(mockWordManager.getWords()).thenReturn(dictionary);
        AtomicInteger counter = new AtomicInteger(1);
        lenient().when(mockWordManager.getRandomWord()).thenAnswer(inv -> "dummy" + counter.getAndIncrement());
    }

    private List<String> setupWeaknessWords(List<TestResult> results) {
        PracticeWords result = practiceService.generatePracticeWords(results);
        List<String> weaknessWords = result.weaknessWords();
        return weaknessWords;
    }

    private List<String> removeDummy(List<String> weaknessWords) {
        return weaknessWords.stream().filter(s -> !s.startsWith("dummy")).toList();
    }
    
    private List<TestResult> createFrequentMistake(int num) {
        List<TestResult> targetResult = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            WordResult testWordResult = new WordResult("freq_" + String.valueOf(i), List.of(new MistakeDetail(MistakeType.SUBSTITUTION, 'a', 'b', 'c', 0)));
            LinkedHashMap<String, WordResult> testResults = new LinkedHashMap<>();
            testResults.put("freq_" + String.valueOf(i), testWordResult);
            TestResult testResult = new TestResult(LocalDateTime.now(), testResults, 0.0, 0.0);
            targetResult.add(testResult);
        }
        return targetResult;
    }

    private List<String> setupFrequentWords(List<TestResult> results) {
        PracticeWords result = practiceService.generatePracticeWords(results);
        List<String> frequentWords = result.frequentMistakeWords();
        return frequentWords;
    }
}
