package application.model;

/**
 * 定数を保存するクラス
 */

public class AppConfig {
    // インスタンス化禁止
    private AppConfig() {};

    // テストの問題数
    public static final int TEST_NUMBERS_OF_QUESTIONS = 10;

    // 単語切り替え時の待機時間(ミリ秒)
    public static final int PAUSE_TIME_MILLIES = 300;

    // 練習の問題数
    public static final int PRACTICE_NUMBERS_OF_QUESTIONS = 10;

    // 各ミスの上位何番目までを取るか
    public static final int MAX_LIMIT_FOR_EACH_TYPE_OF_ERR = 3;

    // wpmと正答率の推移において平均を取らない上限
    public static final int MAX_NOT_AVERAGE_OF_TRANSITION = 30;
}
