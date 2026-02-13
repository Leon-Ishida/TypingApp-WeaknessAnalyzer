package application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final List<String> mockDictionary = List.of(
            "assert", "sample", "sat", "as", "assemble", "save", "apple", "sun", "book", "appart"
    );

    @BeforeEach
    void setup() {
        setupMockWordManager(mockDictionary);
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
        List<String> mockDictionaryComposite = List.of(
            "sub_answer", "sub_shiftamount",
            "trans_ask", "trans_sat",
            "both_sats",
            "trans_extra_as"
        );
        when(mockWordManager.getWords()).thenReturn(mockDictionaryComposite);

        List<TestResult> compositeResults = new ArrayList<>();
        compositeResults.addAll(createSubstitutionMistake('a', 's'));
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
        when(mockWordManager.getWords()).thenReturn(dictionary);
        AtomicInteger counter = new AtomicInteger(1);
        when(mockWordManager.getRandomWord()).thenAnswer(inv -> "dummy" + counter.getAndIncrement());
    }

    private List<String> setupWeaknessWords(List<TestResult> results) {
        PracticeWords result = practiceService.generatePracticeWords(results);
        List<String> weaknessWords = result.weaknessWords();
        return weaknessWords;
    }

    private List<String> removeDummy(List<String> weaknessWords) {
        return weaknessWords.stream().filter(s -> !s.startsWith("dummy")).toList();
    }
}
