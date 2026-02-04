package application.loading;

import application.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * 結果表示画面を表示する前にロード画面を表示するコントローラクラス
 */

public class LoadingController {
    /**
     * result.fxmlに表示するのに時間がかかるため、非同期処理でまずデータを挿入していない状態の画面を作る
     */
    @FXML
    private void initialize() {
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/result/result.fxml"));
                return loader.load();
            }
        };

        //非同期処理が成功したとき、結果表示画面に移動する
        loadTask.setOnSucceeded(event -> {
            Parent resultRoot = loadTask.getValue();
            SceneManager.switchSceneByParent(resultRoot);
        });

        //非同期処理が失敗したとき、ホーム画面に移動する
        loadTask.setOnFailed(event -> {
            loadTask.getException().printStackTrace();
            SceneManager.switchSceneByFileName("/application/home/home.fxml");
        });

        new Thread(loadTask).start();
    }
}
