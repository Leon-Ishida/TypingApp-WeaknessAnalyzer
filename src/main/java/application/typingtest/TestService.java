package application.typingtest;

import application.model.AppConfig;
import application.model.TestResult;
import application.result.FileResultRepository;
import application.service.UserDataManager;

/**
 * テスト終了後、結果と所要時間からミスおよび統計を管理して保存するクラス
 */

public class TestService {
    private FileResultRepository resultManager = new FileResultRepository();
    private TypingAnalyzer analyzer = new TypingAnalyzer();

    /**
     * 所要時間と結果を受け取り、ミスを分析し統計を計算してからjsonファイルに保存するメソッド
     * @param testSession 所要時間と結果を持つクラス
     */
    public void finishTest(TestSession testSession) {
        long usedTimeMillis = testSession.getUsedTimeMillis();
        var rawResult = testSession.getRawResult();

        // WPMを正確に計算するために所要時間の中から待機時間を減らす
        long pauseTimeMillis = (AppConfig.TEST_NUMBERS_OF_QUESTIONS - 1) * AppConfig.PAUSE_TIME_MILLIES;
        long actualUsedTimeMillis = usedTimeMillis - pauseTimeMillis;

        //ガード節
        if (actualUsedTimeMillis < 0) {
            actualUsedTimeMillis = 0;
        }

        //ミスの分析および統計を計算する
        TestResult finalResult = this.analyzer.analyze(rawResult, actualUsedTimeMillis);

        //jsonファイルに保存する
        this.resultManager.save(finalResult);

        //結果表示画面に即時反映させるためにUserDataManagerクラス内のフィールドを更新する
        UserDataManager.getInstance().refreshData();
    }
}
