package application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

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

    @Test
    @DisplayName("置換ミス: aとsが連続する単語を抽出")
    void weaknessWordsSubstitution() {
        List<String> mockDictionary = List.of(
            "assert", "sample", "sat", "as", "assemble", "save", "apple", "sun", "book"
        );
        when(mockWordManager.getWords()).thenReturn(mockDictionary);
        when(mockWordManager.getRandomWord()).thenReturn(
            "dummy1", "dummy2", "dummy3", "dummy4", "dummy5",
            "dummy7", "dummy8", "dummy9", "dummy10",
            "dummy11", "dummy12", "dummy13", "dummy14", "dummy15",
            "dummy16", "dummy17", "dummy18", "dummy19", "dummy20",
            "dummy21", "dummy22", "dummy23", "dummy24", "dummy25"
        );

        List<TestResult> results = createSubstitutionMistake('a', 's');
        PracticeWords result = practiceService.generatePracticeWords(results);
        List<String> weaknessWords = result.weaknessWords();
        List<String> removeDummy = weaknessWords.stream().filter(s -> !s.startsWith("dummy")).toList();

        assertAll("弱点克服用練習単語の検証(置換ミス)",
            () -> assertEquals(3, removeDummy.size(), "置換ミス練習単語は3個であること"),
            () -> assertTrue(removeDummy.stream().allMatch(s -> s.contains("a") && s.contains("s")), "置換ミス練習単語にはaとsが含まれるべき"),
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
}
