package application.typingtest;

import java.util.List;

import application.SceneManager;
import application.common.BaseTypingController;
import application.model.AppConfig;
import javafx.fxml.FXML;

/**
 * テスト画面のコントローラクラス
 */

public class TypingTestController extends BaseTypingController{
    private TestService testService = new TestService();
    private TestSession testSession;

    /**
     * テストモードでは設定不要なため、起動と同時にセッションを開始する
     */
    @FXML
    private void initialize() {
        startSession();
    }

    /**
     * {@inheritDoc}
     * テストモードではWordManagereから全単語を取得し、シャッフルして単語リストとして設定する
     */
    @Override
    protected List<String> loadWord() {
        wordManager.shuffleWords();
        return wordManager.getWords();
    }

    /**
     * {@inheritDoc}
     * テストモードの初期化を担当し、TestSessionをインスタンス化する
     */
    @Override
    protected void initializeByMode() {
        questionCount.setText("問題: 1 / " + AppConfig.TEST_NUMBERS_OF_QUESTIONS);
        this.testSession = new TestSession();
    }

    /**
     * {@inheritDoc}
     * テストモードではセッションに結果を保存した後、問題数のラベルの更新を行う
     * また、最終問題の場合次へ進むボタンのテキストをFinishにする
     */
    @Override
    protected void onWordCorrected(String currentWord, String userAnswer) {
        this.testSession.addResult(currentWord, userAnswer);

        if (count + 1 < AppConfig.TEST_NUMBERS_OF_QUESTIONS) {
            questionCount.setText("問題: " + (count + 2) + " / " + AppConfig.TEST_NUMBERS_OF_QUESTIONS);
        }

        if (count == AppConfig.TEST_NUMBERS_OF_QUESTIONS - 2) {
            nextButton.setText("Finish");
        }
    }
    
    /**
     * {@inheritDoc}
     * 最終問題か判定する
     * AppConfigで定義されたテストの問題数で判定する
     */
    @Override
    protected boolean isLastWord() {
        return count >= AppConfig.TEST_NUMBERS_OF_QUESTIONS;
    }

    /**
     * {@inheritDoc}
     * セッション終了時に行われる処理を担当する
     * TestServiceを呼び出して結果を保存し、結果画面に遷移する
     */
    @Override
    protected void finishSession() {
        this.testService.finishTest(testSession);
        SceneManager.switchSceneByFileName("/application/loading/loading.fxml");
        
    }
}
