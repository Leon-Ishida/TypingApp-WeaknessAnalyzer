package application.result;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.model.FilteredResult;
import application.model.TestResult;

public class ResultFilterTest {
    private TestResult createResult(LocalDateTime time) {
        return new TestResult(time, null, 0.0, 0.0);
    }

    @Test
    @DisplayName("今日のデータが正しく抽出されること")
    void testFilterToday() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 2, 5, 12, 0);

        TestResult insideFront = createResult(LocalDateTime.of(2026, 2, 5, 0, 0, 0, 0));
        TestResult insideBack = createResult(LocalDateTime.of(2026, 2, 5, 23, 59, 59, 999));
        TestResult outsideFront = createResult(LocalDateTime.of(2026, 2, 4, 23, 59, 59, 999));
        TestResult outsideBack = createResult(LocalDateTime.of(2026, 2, 6, 0, 0, 0, 0));

        List<TestResult> testList = Arrays.asList(insideFront, insideBack, outsideFront, outsideBack);
        
        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.todayResults().contains(insideFront));
        assertTrue(result.todayResults().contains(insideBack));
        assertFalse(result.todayResults().contains(outsideFront));
        assertFalse(result.todayResults().contains(outsideBack));
    }

    @Test
    @DisplayName("今週のデータが正しく抽出されること")
    void testFilterThisWeek() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 2, 6, 12, 0);
        
        TestResult insideFront = createResult(LocalDateTime.of(2026, 2, 2, 0, 0, 0));
        TestResult insideBack = createResult(LocalDateTime.of(2026, 2, 8, 23, 59, 59, 999));
        TestResult outsideFront = createResult(LocalDateTime.of(2026, 2, 1, 23, 59, 59, 999));
        TestResult outsideBack = createResult(LocalDateTime.of(2026, 2, 9, 0, 0, 0, 0));

        List<TestResult> testList = Arrays.asList(insideFront, insideBack, outsideFront, outsideBack);

        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.thisWeekResults().contains(insideFront));
        assertTrue(result.thisWeekResults().contains(insideBack));
        assertFalse(result.thisWeekResults().contains(outsideFront));
        assertFalse(result.thisWeekResults().contains(outsideBack));
    }

    @Test
    @DisplayName("今月のデータが正しく抽出されること")
    void testFilterThisMonth() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 2, 6, 12, 0, 0);

        TestResult insideFront = createResult(LocalDateTime.of(2026, 2, 1, 0, 0, 0));
        TestResult insideBack = createResult(LocalDateTime.of(2026, 2, 28, 23, 59, 59, 999));
        TestResult outsideFront = createResult(LocalDateTime.of(2026, 1, 31, 23, 59, 59, 999));
        TestResult outsideBack = createResult(LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0));

        List<TestResult> testList = Arrays.asList(insideFront, insideBack, outsideFront, outsideBack);

        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.thisMonthResults().contains(insideFront));
        assertTrue(result.thisMonthResults().contains(insideBack));
        assertFalse(result.thisMonthResults().contains(outsideFront));
        assertFalse(result.thisMonthResults().contains(outsideBack));
    }

    @Test
    @DisplayName("うるう年の2/29を含んだ週が正しく抽出されること")
    void testFilterLeapYearWeek() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 2, 29, 12, 0);

        TestResult insideFront = createResult(LocalDateTime.of(2024, 2, 26, 0, 0, 0, 0));
        TestResult insideCenter = createResult(LocalDateTime.of(2024, 3, 1, 12, 0));
        TestResult insideBack = createResult(LocalDateTime.of(2024, 3, 3, 23, 59, 59, 999));
        TestResult outsideFront = createResult(LocalDateTime.of(2024, 2, 25, 23, 59, 59, 999));
        TestResult outsideBack = createResult(LocalDateTime.of(2024, 3, 4, 0, 0, 0, 0));

        List<TestResult> testList = Arrays.asList(insideFront, insideCenter, insideBack, outsideFront, outsideBack);

        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.thisWeekResults().contains(insideFront));
        assertTrue(result.thisWeekResults().contains(insideCenter));
        assertTrue(result.thisWeekResults().contains(insideBack));
        assertFalse(result.thisWeekResults().contains(outsideFront));
        assertFalse(result.thisWeekResults().contains(outsideBack));
    }

    @Test
    @DisplayName("うるう年の2月が正しく抽出されること")
    void testFilterLeapYearMonth() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 2, 6, 12, 0, 0);

        TestResult insideFront = createResult(LocalDateTime.of(2024, 2, 1, 0, 0, 0));
        TestResult insideBack = createResult(LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999));
        TestResult outsideFront = createResult(LocalDateTime.of(2024, 1, 31, 23, 59, 59, 999));
        TestResult outsideBack = createResult(LocalDateTime.of(2024, 3, 1, 0, 0, 0, 0));

        List<TestResult> testList = Arrays.asList(insideFront, insideBack, outsideFront, outsideBack);

        FilteredResult result = ResultFilter.filterAll(testList, fixedNow);

        assertTrue(result.thisMonthResults().contains(insideFront));
        assertTrue(result.thisMonthResults().contains(insideBack));
        assertFalse(result.thisMonthResults().contains(outsideFront));
        assertFalse(result.thisMonthResults().contains(outsideBack));
    }

    @Test
    @DisplayName("入力がnullまたは空リストの場合、結果も空になること")
    void testNonOrNullData() {
        FilteredResult resultFromNull = ResultFilter.filterAll(null);

        assertAll("Null入力時の全フィールド検証",
            () -> assertNull(resultFromNull.lastResult(), "直近の結果はnullであるべき"),
            () -> assertTrue(resultFromNull.todayResults().isEmpty(), "今日のリストは空であるべき"),
            () -> assertTrue(resultFromNull.thisWeekResults().isEmpty(), "今週のリストは空であるべき"),
            () -> assertTrue(resultFromNull.thisMonthResults().isEmpty(), "今月のリストは空であるべき"),
            () -> assertTrue(resultFromNull.allResults().isEmpty(), "全結果リストは空であるべき"),
            () -> assertTrue(resultFromNull.recent10Results().isEmpty(), "直近10回のリストは空であるべき")
        );

        FilteredResult resultEmpty = ResultFilter.filterAll(Collections.emptyList());
        assertAll("空リスト入力時の全フィールド検証",
            () -> assertNull(resultEmpty.lastResult(), "直近の結果はnullであるべき"),
            () -> assertTrue(resultEmpty.todayResults().isEmpty(), "今日のリストは空であるべき"),
            () -> assertTrue(resultEmpty.thisWeekResults().isEmpty(), "今週のリストは空であるべき"),
            () -> assertTrue(resultEmpty.thisMonthResults().isEmpty(), "今月のリストは空であるべき"),
            () -> assertTrue(resultEmpty.allResults().isEmpty(), "全結果リストは空であるべき"),
            () -> assertTrue(resultEmpty.recent10Results().isEmpty(), "直近10回のリストは空であるべき")
        );
    }
}
