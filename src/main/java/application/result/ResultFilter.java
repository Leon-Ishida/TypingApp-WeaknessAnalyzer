package application.result;

import java.util.List;

import application.model.FilteredResult;
import application.model.TestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

/**
 * テスト結果のリストを各期間ごとにフィルタリングするユーティリティクラス
 */

public class ResultFilter {
    /**
     * 全テスト結果のリストを受け取り期間ごとに分類した結果オブジェクトを作る
     * <p>具体的には、「前回」「今日」「今週」「今月」「直近10回」のリストを作成し{@link application.model.FilteredResult}レコードとして返す</p>
     * @param allResults 全テスト結果のリスト
     * @return 各期間ごとに分類されたリストが格納されたFilteredResultオブジェクト
     * 引数のリストがnullまたは空の場合、すべてのリストが空のFilteredResultオブジェクトを返す
     */
    public static FilteredResult filterAll(List<TestResult> allResults) {
        //ガード節
        if (allResults == null || allResults.isEmpty()) {
            return new FilteredResult(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
        
        List<TestResult> todayResults = new ArrayList<>();
        List<TestResult> thisWeekResults = new ArrayList<>();
        List<TestResult> thisMonthResults = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate firstOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (TestResult eachResult : allResults) {
            LocalDate resultDate = eachResult.timestamp().toLocalDate();

            //今日の結果を取得
            if (resultDate.equals(today)) {
                todayResults.add(eachResult);
            }

            //今週の結果を取得
            if (!resultDate.isBefore(firstOfWeek) && !resultDate.isAfter(lastOfWeek)) {
                thisWeekResults.add(eachResult);
            }

            //今月の結果を取得
            if (YearMonth.from(resultDate).equals(YearMonth.now())) {
                thisMonthResults.add(eachResult);
            }
        }

        //直近10回の結果を取得
        int size = allResults.size();
        int startIndex = Math.max(0, size - 10);
        List<TestResult> recent10Results = new ArrayList<>(allResults.subList(startIndex, size));

        //前回の結果を取得
        TestResult lastResult = allResults.get(allResults.size() - 1);
    
        return new FilteredResult(lastResult, todayResults, thisWeekResults, thisMonthResults, allResults, recent10Results);
    }
}
