package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * アプリケーションを起動するmainクラス
 */


public class Main extends Application {
	/**
	 * 最初の画面であるホーム画面を読み込み、{@link SceneManager}にStageを登録する
	 * @param primaryStage アプリケーションのメインウィンドウ
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("/application/home/home.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			SceneManager.setStage(primaryStage);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * アプリケーションを起動するJavaのmainメソッド
	 * @param args 起動時変数
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
