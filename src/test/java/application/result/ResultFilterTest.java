package application.result;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.model.FilteredResult;
import application.model.TestResult;

public class ResultFilterTest {
    @Test
    @DisplayName("今日のデータが正しく抽出されること")
    void testFilterToday() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 2, 5, 12, 0);

        TestResult insideFront = new TestResult(LocalDateTime.of(2026, 2, 5, 0, 0, 0, 0), null, 0.0, 0.0);
        TestResult insideBack = new TestResult(LocalDateTime.of(2026, 2, 5, 23, 59, 59, 999), null, 0.0, 0.0);
        TestResult outsideFront = new TestResult(LocalDateTime.of(2026, 2, 4, 23, 59, 59, 999), null, 0.0, 0.0);
        TestResult outsideBack = new TestResult(LocalDateTime.of(2026, 2, 6, 0, 0, 0, 0), null, 0.0, 0.0);

        List<TestResult> testList = Arrays.asList(insideFront, insideBack, outsideFront, outsideBack);
        
        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.todayResults().contains(insideFront));
        assertTrue(result.todayResults().contains(insideBack));
        assertFalse(result.todayResults().contains(outsideFront));
        assertFalse(result.todayResults().contains(outsideBack));
    }
}
