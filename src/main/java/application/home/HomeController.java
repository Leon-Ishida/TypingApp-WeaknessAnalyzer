package application.home;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * ホーム画面のコントローラクラス
 */

public class HomeController {
	@FXML private Button toTestButton; //テスト画面に進むボタン
	@FXML private Button toResultButton; //結果画面に進むボタン
	@FXML private Button toPracticeSelectionButton; //練習モード選択画面に進むボタン

	/**
	 * テスト画面への遷移を担当するメソッド
	 * @param event toTestButtonによるアクションイベント
	 */
    @FXML
    private void switchHomeToTest(ActionEvent event) {
        SceneManager.switchSceneByFileName("/application/typingtest/typingTest.fxml");
    }

	/**
	 * 結果表示画面への遷移を担当するメソッド
	 * 結果表示画面を表示するのは時間がかかるため間にロード画面を挟む
	 * @param event toResultButtonによるアクションイベント
	 */
	@FXML
	private void switchHomeToResult(ActionEvent event) {
		SceneManager.switchSceneByFileName("/application/loading/loading.fxml");
	}

	/**
	 * 練習モード選択画面への遷移を担当するメソッド
	 * @param event toPracticeSelectionButtonによるアクションイベント
	 */
	@FXML
	private void switchHomeToPracticeSelection(ActionEvent event) {
		SceneManager.switchSceneByFileName("/application/practice/practiceSelection.fxml");
	}
}
