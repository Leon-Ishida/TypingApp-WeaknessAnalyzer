package application.practice;

import application.model.Period;
import application.model.PracticeMode;
import application.model.PracticeConfig;

import java.io.IOException;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

public class PracticeSelectionController {
    @FXML private ChoiceBox<Period> periodChoiceBox;
    @FXML private Button weakness;
    @FXML private Button frequentMistake;
    @FXML private Button endless;
    @FXML private Button toHomeButton;

    Period period; //選択した期間
    PracticeMode mode; //選択したモード

    /**
     * ChoiceBoxのセットアップするためのメソッド
     */
    @FXML
    private void initialize() {
        periodChoiceBox.getItems().addAll(Period.values());
        periodChoiceBox.setValue(Period.LAST);
    }

    /**
     * 弱点克服を選んだ時の処理を担当するメソッド
     * @param event weaknessによるアクションイベント
     */
    @FXML
    private void selectWeakness(ActionEvent event) {
        this.mode = PracticeMode.WEAKNESS;
        startPractice();
    }

    /**
     * 頻出ミスを選んだ時の処理を担当するメソッド
     * @param event frequentMistakeによるアクションイベント
     */
    @FXML
    private void selectFrequentMistake(ActionEvent event) {
        this.mode = PracticeMode.FREQUENT_MISTAKE;
        startPractice();
    }

    /**
     * エンドレスを選んだ時の処理を担当するメソッド
     * @param event endlessによるアクションイベント
     */
    @FXML
    private void selectEndless(ActionEvent event) {
        this.mode = PracticeMode.ENDLESS;
        startPractice();
    }

    /**
     * 選択した期間とモードにしたがって練習を開始する共通メソッド
     */
    private void startPractice() {
        this.period = periodChoiceBox.getValue();
        if (period == null) {
            period = Period.LAST;
        }
        PracticeConfig config = new PracticeConfig(period, mode);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/practice/practice.fxml"));
            Parent root = loader.load();
            PracticeController controller = loader.getController();
            controller.setupPractice(config);
            SceneManager.switchSceneByParent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ホーム画面に戻る処理
     * @param event toHomeButtonによるアクションイベント
     */
    @FXML
    private void switchSelectionToHome(ActionEvent event) {
        SceneManager.switchSceneByFileName("/application/home/home.fxml");
    }
}
