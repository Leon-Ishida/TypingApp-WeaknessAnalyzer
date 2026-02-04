package application.model;

import java.util.List;

/**
 * 各期間のテスト結果のリストを格納するデータクラス
 * @param lastResult 前回のテスト結果
 * @param todayResults 今日のテスト結果
 * @param thisWeekResults 今週のテスト結果
 * @param thisMonthResults 今月のテスト結果
 * @param allResults 全期間のテスト結果
 * @param recent10Results 直近10回のテスト結果
 */

public record FilteredResult(
    TestResult lastResult,
    List<TestResult> todayResults,
    List<TestResult> thisWeekResults,
    List<TestResult> thisMonthResults,
    List<TestResult> allResults,
    List<TestResult> recent10Results
) {}
