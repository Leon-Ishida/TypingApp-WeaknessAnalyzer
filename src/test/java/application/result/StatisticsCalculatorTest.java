package application.result;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.model.StatisticsResult;
import application.model.TestResult;

public class StatisticsCalculatorTest {
    @Test
    @DisplayName("正常な入力時、平均WPMと平均正答率が正しく計算されること")
    void testWpmAndAccuracy() {
        List<TestResult> testResultsList = new ArrayList<>();
        testResultsList.add(createTestResult(3.0, 0.8));
        testResultsList.add(createTestResult(9.0, 0.4));
        testResultsList.add(createTestResult(15.0, 0.3));
        StatisticsResult statistics = StatisticsCalculator.calculate(testResultsList);

        assertAll("正常な入力時、平均WPMと平均正答率が正しく計算されること",
            () -> assertEquals(9.0, statistics.averageWpm(), "平均WPMは9.0であること"),
            () -> assertEquals(0.5, statistics.averageAccuracy(), "平均正答率は0.5であること")
        );
    }

    @Test
    @DisplayName("結果が小数第3位で丸められること")
    void testRounding() {
        List<TestResult> testResultsList = new ArrayList<>();
        testResultsList.add(createTestResult(1.0, 0.2));
        testResultsList.add(createTestResult(2.0, 0.2));
        testResultsList.add(createTestResult(4.0, 0.1));
        StatisticsResult statistics = StatisticsCalculator.calculate(testResultsList);

        assertAll("平均WPMと平均正答率がどちらも小数第3位に丸められること",
            () -> assertEquals(2.333, statistics.averageWpm(), "平均WPMは2.333であること"),
            () -> assertEquals(0.167, statistics.averageAccuracy(), "平均正答率は0.167であること")
        );
    }
    
    @Test
    @DisplayName("入力が1件の時、WPMと正答率がそのまま返ること")
    void testExistOneResult() {
        List<TestResult> testResultList = new ArrayList<>();
        testResultList.add(createTestResult(5.0, 0.5));
        StatisticsResult statistics = StatisticsCalculator.calculate(testResultList);

        assertAll("入力が1件の時、WPMと正答率がそのまま返ること",
            () -> assertEquals(5.0, statistics.averageWpm(), "平均WPMは5.0であること"),
            () -> assertEquals(0.5, statistics.averageAccuracy(), "平均正答率は0.5であること")
        );
    }

    @Test
    @DisplayName("入力リストがnullの時、ガード節が働くこと")
    void testNullInput() {
        StatisticsResult statistics = StatisticsCalculator.calculate(null);

        assertAll("入力がnullの時、平均WPMと平均正答率が共に0.0になること",
            () -> assertEquals(0.0, statistics.averageWpm(), "平均WPMは0.0であること"),
            () -> assertEquals(0.0, statistics.averageAccuracy(), "平均正答率は0.0であること")
        );
    }

    @Test
    @DisplayName("入力リストが空の時、ガード節が働くこと")
    void testEmptyInput() {
        List<TestResult> testResultList = new ArrayList<>();
        StatisticsResult statistics = StatisticsCalculator.calculate(testResultList);

        assertAll("入力が空の時、平均WPMと平均正答率が共に0.0になること",
            () -> assertEquals(0.0, statistics.averageWpm(), "平均WPMは0.0であること"),
            () -> assertEquals(0.0, statistics.averageAccuracy(), "平均正答率は0.0であること")
        );
    }

    private TestResult createTestResult(double wpm, double accuracy) {
        return new TestResult(LocalDateTime.now(), null, wpm, accuracy);
    }
}
