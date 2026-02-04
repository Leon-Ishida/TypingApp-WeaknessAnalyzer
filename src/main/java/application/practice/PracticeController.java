package application.practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.SceneManager;
import application.common.BaseTypingController;
import application.model.AppConfig;
import application.model.FilteredResult;
import application.model.PracticeConfig;
import application.model.PracticeMode;
import application.model.PracticeWords;
import application.model.TestResult;
import application.service.PracticeService;
import application.service.UserDataManager;
import javafx.scene.control.Alert;

/**
 * 練習モードを選択する画面のコントローラクラス
 */

public class PracticeController extends BaseTypingController {
    PracticeService practiceService;
    PracticeConfig practiceConfig;

    /**
     * 設定を受け取り、練習を開始するメソッド
     * @param config 対象期間とモードの設定
     */
    public void setupPractice(PracticeConfig config) {
        this.practiceConfig = config;
        startSession();
    }
    
    /**
     * {@inheritDoc}
     * 設定に基づいて単語リストを生成する
     */
    @Override
    protected List<String> loadWord() {
        if (practiceConfig == null) {
            return Collections.emptyList();

        }
        this.practiceService = new PracticeService(wordManager);

        FilteredResult filteredResult = UserDataManager.getInstance().getFilteredResult();
        List<TestResult> targetResults = switch (practiceConfig.period()) {
            case TODAY -> filteredResult.todayResults();
            case THISWEEK -> filteredResult.thisWeekResults();
            case THISMONTH -> filteredResult.thisMonthResults();
            case ALLTIME -> filteredResult.allResults();
            case LAST -> filteredResult.lastResult() != null ? List.of(filteredResult.lastResult()) : Collections.emptyList();
        };
        
        List<String> finalWordList = new ArrayList<>();
        if (practiceConfig.mode() == PracticeMode.ENDLESS) {
            wordManager.shuffleWords();
            finalWordList.addAll(wordManager.getWords());
        } else {
            PracticeWords words = practiceService.generatePracticeWords(targetResults);
            switch (practiceConfig.mode()) {
                case WEAKNESS:
                    finalWordList.addAll(words.weaknessWords());
                    break;
                case FREQUENT_MISTAKE:
                    finalWordList.addAll(words.frequentMistakeWords());
                    break;
                default:
                    break;
            }
            Collections.shuffle(finalWordList);
        }

        if (finalWordList.isEmpty()) {
                finalWordList.add(wordManager.getRandomWord());
        }

        return finalWordList;
    }

    /**
     * {@inheritDoc}
     * 練習のモードに応じたラベルの初期化を行う
     */
    @Override
    protected void initializeByMode() {
        if (wordList == null || wordList.isEmpty()) {
            questionCount.setText("準備中");
            return;
        }
        
        if (practiceConfig.mode() == PracticeMode.ENDLESS) {
            questionCount.setText("問題: 1 / ∞");
        } else {
            questionCount.setText("問題: 1 / " + AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS);
        }
        nextButton.setText("Next");
    }

    /**
     * {@inheritDoc}
     * 練習モードでは問題数のラベルの更新のみを行う
     * また弱点克服または頻出ミスモードにおいて、最終問題の場合次へ進むボタンのテキストをFinishにする
     */
    @Override
    protected void onWordCorrected(String currentWord, String userAnswer) {
        if (practiceConfig.mode() == PracticeMode.ENDLESS) {
            questionCount.setText("問題: " + (count + 2) + " / ∞");
        } else {
            if (count + 1 < AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS) {
                questionCount.setText("問題: " + (count + 2) + " / " + AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS);
            }
            if (count == AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS - 2) {
                nextButton.setText("Finish");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 最終問題か判定する
     * AppConfigで定義された練習の問題数で判定する
     */
    @Override
    protected boolean isLastWord() {
        if (practiceConfig.mode() != null && practiceConfig.mode() == PracticeMode.ENDLESS) {
            return false;
        } else {
            return count >= AppConfig.PRACTICE_NUMBERS_OF_QUESTIONS;
        }
    }

    /**
     * {@inheritDoc}
     * 終了時の処理を担当する
     * ダイアログを出してホームに戻る
     */
    @Override
    protected void finishSession() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("練習終了");
        alert.setHeaderText(null);
        alert.setContentText("ホームに戻ります");
        alert.showAndWait();

        SceneManager.switchSceneByFileName("/application/home/home.fxml");
    }
}
