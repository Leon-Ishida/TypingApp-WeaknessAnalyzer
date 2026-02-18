package application.typingtest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
            () -> assertEquals(0.5, result.wpm(), "WPMは0.5であるべき")
        );
    }
}
