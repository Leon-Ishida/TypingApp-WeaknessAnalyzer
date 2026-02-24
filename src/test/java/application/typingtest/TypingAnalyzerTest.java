package application.typingtest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.model.TestResult;

public class TypingAnalyzerTest {
    private final TypingAnalyzer analyzer = new TypingAnalyzer();

    @Test
    @DisplayName("全問正解の時、WPMと正答率が正しいこと")
    void testAllCorrect() {
        LinkedHashMap<String, String> rawResult = new LinkedHashMap<>();
        rawResult.put("apple", "apple");
        rawResult.put("books", "books");
        TestResult result = analyzer.analyze(rawResult, 60000L);
        
        assertAll("全問正解時のWPMと正答率検証",
            () -> assertEquals(1.0, result.accuracy(), "正答率は1.0であるべき"),
            () -> assertEquals(2.0, result.wpm(), "WPMは2.0であるべき")
        );
    }

    @Test
    @DisplayName("全問不正解の時、WPMと正答率が正しいこと")
    void testAllMistake() {
        LinkedHashMap<String, String> rawResult = new LinkedHashMap<>();
        rawResult.put("apple", "banana");
        rawResult.put("book", "paper");
        TestResult result = analyzer.analyze(rawResult, 60000L);

        assertAll("全問不正解時のWPMと正答率検証",
            () -> assertEquals(0.0, result.accuracy(), "正答率は0.0であるべき"),
            () -> assertEquals(0.0, result.wpm(), "WPMは0.0であるべき")
        );
    }

    @Test
    @DisplayName("正解不正解どちらもあるとき、WPMと正答率が正しいこと")
    void testMixCorrectAndMistake() {
        LinkedHashMap<String, String> rawResult = new LinkedHashMap<>();
        rawResult.put("apple", "apple");
        rawResult.put("book", "paper");
        TestResult result = analyzer.analyze(rawResult, 10000L);

        assertAll("正解と不正解が混ざっているときのWPMと正答率検証",
            () -> assertEquals(0.5, result.accuracy(), "正答率は0.5であるべき"),
            () -> assertEquals(6.0, result.wpm(), "WPMは6.0であるべき")
        );
    }

    @Test
    @DisplayName("所要時間が0以下の時、WPMが0.0になること")
    void testGuardOfWpm() {
        LinkedHashMap<String, String> rawResult = new LinkedHashMap<>();
        rawResult.put("apple", "apple");
        rawResult.put("book", "book");
        TestResult result = analyzer.analyze(rawResult, 0L);

        assertEquals(0.0, result.wpm(), "WPMは0.0であるべき");
    }

    @Test
    @DisplayName("テストした単語のMapがnullの時、例外処理をすること")
    void testNullInput() {
        LinkedHashMap<String, String> rawResult = null;
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze(rawResult, 60000L));
    }

    @Test
    @DisplayName("テストした単語のMapが空の時、例外処理をすること")
    void testEmptyInput() {
        LinkedHashMap<String, String> rawResult = new LinkedHashMap<>();
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze(rawResult, 60000L));
    }
}
