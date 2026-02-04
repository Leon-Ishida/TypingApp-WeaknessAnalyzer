package application.common;

import java.util.List;

import application.SceneManager;
import application.model.AppConfig;
import application.service.WordManager;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public abstract class BaseTypingController {
    @FXML protected Button toHomeButton; //ホーム画面に戻るボタン
    @FXML protected Button nextButton; //次の問題に進むボタン
    @FXML protected Label word; //問題の単語
    @FXML protected Label questionCount; //問題数を表示するラベル
    @FXML protected TextField field;  //ユーザーの入力を受け入れるパスワードフィールド
    @FXML protected VBox vBoxForCountdown; //カウントダウンを表示ラベルが入ったVBox
    @FXML protected Label countdown; //カウントダウンを表示するラベル

    protected int count = 0; //現在の問題数をカウントする
    protected WordManager wordManager = new WordManager();
    protected List<String> wordList;

    /**
     * タイピングセッションを開始するメソッド
     * 単語リストのロードとモード固有の初期化、及び最初の単語の表示と入力フィールドにフォーカスを当てる
     */
    protected void startSession() {
        this.wordList = loadWord();
        if (wordList != null && !wordList.isEmpty()) {
            word.setText(wordList.get(count));
        } else {
            word.setText("No Words");
        }
        startCountdown();
    }

    /**
     * nextButtonが押された場合またはEnterキーが押された場合呼び出されるメソッド
     * 正誤判定し、正誤に応じて問題の色を変化させ、一時停止をし終了後に入力フィールドをクリアする
     * モード固有のデータ処理を抽象メソッドであるonWordCorrectedメソッドで行う
     * 最終問題ではない場合、次のお題を設定し入力フィールドにフォーカスを当てる
     * 最終問題の場合抽象メソッドであるfinishSessionをメソッドを呼び出してモードによる終了処理を呼び出す
     * @param event nextButtonまたはEnterキーによるアクションイベント
     */
    @FXML
    protected void nextWord(ActionEvent event) {
        String currentWord = word.getText();
        String userAnswer = field.getText();

        onWordCorrected(currentWord, userAnswer);

        boolean isCorrect = userAnswer.equals(currentWord);

        nextButton.setDisable(true);
        field.setDisable(true);

        if (isCorrect) {
            word.setTextFill(Color.GREEN);
        } else {
            word.setTextFill(Color.RED);
        }

        PauseTransition pause = new PauseTransition(Duration.millis(AppConfig.PAUSE_TIME_MILLIES));

        pause.setOnFinished(e -> {
            word.setTextFill(Color.BLACK);

            field.clear();
            count++;
            if (!isLastWord()) {
                word.setText(wordList.get(count % wordList.size()));
                nextButton.setDisable(false);
                field.setDisable(false);
                field.requestFocus();
            } else {
                Platform.runLater(() -> finishSession());
            }
        });

        pause.play();
    }

    /**
     * 開始前にカウントダウンするメソッド
     */
    protected void startCountdown() {
        field.setDisable(true);
        countdown.setVisible(true);
        countdown.setText("3");

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> countdown.setText("3")),
            new KeyFrame(Duration.seconds(1), e -> countdown.setText("2")),
            new KeyFrame(Duration.seconds(2), e -> countdown.setText("1")),
            new KeyFrame(Duration.seconds(3), e -> countdown.setText("GO"))
        );

        timeline.setCycleCount(1);

        timeline.setOnFinished(e -> {
            PauseTransition postGoDelay = new PauseTransition(Duration.millis(500));
            postGoDelay.setOnFinished(ev -> {
                vBoxForCountdown.setVisible(false);
                countdown.setVisible(false);
                field.setDisable(false);
                field.requestFocus();

                initializeByMode();
            });

            postGoDelay.play();
        });

        timeline.play();
    }

    /**
     * セッションで使用する単語リストを取得する抽象メソッド
     * @return 問題に使用する単語リスト
     */
    protected abstract List<String> loadWord();

    /**
     * テストモードか練習モードで変化する初期化処理を担当する抽象メソッド
     */
    protected abstract void initializeByMode();

    /**
     * セッションに結果を保存するなどモードによるデータ処理を行う抽象メソッド
     * @param currentWord 正解の単語
     * @param userAnswer ユーザーの入力
     */
    protected abstract void onWordCorrected(String currentWord, String userAnswer);

    /**
     * 最後の問題かどうかを返す抽象メソッド
     * @return 最終問題かどうかの真偽値
     */
    protected abstract boolean isLastWord();

    /**
     * セッション終了時に行われる処理を担当する抽象メソッド
     */
    protected abstract void finishSession();

    /**
     * ホーム画面への遷移を担当するメソッド
     * @param event toHomeButtonによるアクションイベント
     */
    @FXML
    private void switchTestToHome(ActionEvent event) {
        SceneManager.switchSceneByFileName("/application/home/home.fxml");
    }
}
