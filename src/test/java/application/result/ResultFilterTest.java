package application.result;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        assertAll("今日のデータの境界値検証",
            () -> assertTrue(result.todayResults().contains(insideFront), "日初めが含まれるべき"),
            () -> assertTrue(result.todayResults().contains(insideBack), "日末が含まれるべき"),
            () -> assertFalse(result.todayResults().contains(outsideFront), "昨日末が含まれないべき"),
            () -> assertFalse(result.todayResults().contains(outsideBack), "翌日初めが含まれないべき")
        );

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

        assertAll("今週のデータの境界値検証",
            () -> assertTrue(result.thisWeekResults().contains(insideFront), "今週初めが含まれるべき"),
            () -> assertTrue(result.thisWeekResults().contains(insideBack), "今週末が含まれるべき"),
            () -> assertFalse(result.thisWeekResults().contains(outsideFront), "先週末が含まれないべき"),
            () -> assertFalse(result.thisWeekResults().contains(outsideBack), "翌週初めが含まれないべき")
        );
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

        assertAll("今月のデータの境界値検証", 
            () -> assertTrue(result.thisMonthResults().contains(insideFront), "今月初めは含まれるべき"),
            () -> assertTrue(result.thisMonthResults().contains(insideBack), "今月末は含まれるべき"),
            () -> assertFalse(result.thisMonthResults().contains(outsideFront), "先月末は含まれないべき"),
            () -> assertFalse(result.thisMonthResults().contains(outsideBack), "翌月初めは含まれないべき")
        );
        
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

        assertAll("うるう年の今週のデータの境界値検証",
            () -> assertTrue(result.thisWeekResults().contains(insideFront), "今週初めが含まれるべき"),
            () -> assertTrue(result.thisWeekResults().contains(insideCenter), "月が変わっても含まれるべき"),
            () -> assertTrue(result.thisWeekResults().contains(insideBack), "今週末が含まれるべき"),
            () -> assertFalse(result.thisWeekResults().contains(outsideFront), "先週末が含まれないべき"),
            () -> assertFalse(result.thisWeekResults().contains(outsideBack), "翌週初めが含まれないべき")
        );
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

        assertAll("うるう年の今月のデータの境界値検証", 
            () -> assertTrue(result.thisMonthResults().contains(insideFront), "今月初めは含まれるべき"),
            () -> assertTrue(result.thisMonthResults().contains(insideBack), "今月末は含まれるべき"),
            () -> assertFalse(result.thisMonthResults().contains(outsideFront), "先月末は含まれないべき"),
            () -> assertFalse(result.thisMonthResults().contains(outsideBack), "翌月初めは含まれないべき")
        );
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

    @Test
    @DisplayName("データ件数に応じた直近10回のデータ抽出と最新結果の取得が正しく行われること")
    void testRecent10AndLastResult() {
        LocalDateTime now = LocalDateTime.now();

        List<TestResult> smallList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            smallList.add(new TestResult(now, null, (i + 1) * 1.0, 0.0));
        }
        FilteredResult smallResult = ResultFilter.filterAll(smallList);
        assertAll("データが10件未満の場合の検証",
            () -> assertEquals(5, smallResult.recent10Results().size(), "5件すべてが返されるべき"),
            () -> assertEquals(1.0, smallResult.recent10Results().get(0).wpm(), "リストの先頭は1件目であるべき"),
            () -> assertEquals(5.0, smallResult.lastResult().wpm(), "直近は5件目であるべき")
        );

        List<TestResult> largeList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            largeList.add(new TestResult(now, null, (i + 1) * 1.0, 0.0));
        }
        FilteredResult largeResult = ResultFilter.filterAll(largeList);
        assertAll("データが10件以上の場合の検証",
            () -> assertEquals(10, largeResult.recent10Results().size(), "10件に削減されていなければならない"),
            () -> assertEquals(6.0, largeResult.recent10Results().get(0).wpm(), "リストの先頭は6件目であるべき"),
            () -> assertEquals(15.0, largeResult.recent10Results().get(9).wpm(), "リストの末尾は15件目であるべき"),
            () -> assertEquals(15.0, largeResult.lastResult().wpm(), "直近は15件目であるべき")
        );
    }
}
