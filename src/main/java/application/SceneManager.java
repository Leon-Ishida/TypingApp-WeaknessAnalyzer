package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * 画面遷移を管理するユーティリティクラス
 * アプリケーションを起動時に{@link Main}クラスからウィンドウであるprimaryStageの参照を受け取り、
 * それを静的に保持することで、どのコントローラクラスからでも呼び出せるようにする
 */

public class SceneManager {
    private static Stage primaryStage;

    /**
     * ユーティリティクラスのため、初期化を禁止する
     */
    private SceneManager() {}

    /**
     * primaryStageを初期化するメソッド
     * 通常{@link application.Main#start(Stage)}メソッドから呼び出される
     * @param stage アプリケーションのprimaryStage
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * 引数に受け取るfxmlファイル名の基づいて画面を切り替えるメソッド
     * このメソッドは標準的な画面遷移の時に使われる
     * @param fxmlFileName 表示する画面に対応するfxmlファイル名
     */
    public static void switchSceneByFileName(String fxmlFileName) {
        try {
			BorderPane root = (BorderPane)FXMLLoader.load(SceneManager.class.getResource(fxmlFileName));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(SceneManager.class.getResource("/application/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * すでにロード済みの{@link Parent}オブジェクトを使って画面を切り替えるメソッド
     * このメソッドは{@link application.loading.LoadingController}のように、
     * 非同期処理でfxmlを読み込み、完了後にその実体を渡して画面遷移をする場合に使われる
     * @param root 遷移先のシーンとして設定するParentノード
     */
    public static void switchSceneByParent(Parent root) {
        try {
            Scene scene = new Scene(root);
            scene.getStylesheets().add(SceneManager.class.getResource("/application/application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
