package application.typingtest;

import java.util.LinkedHashMap;

/**
 * テスト実施中、正解と解答を保存して所要時間とともに渡すクラス
 */

public class TestSession {
    private final long startTime;
    private final LinkedHashMap<String, String> rawResult;

    /**
     * 始めた時刻を取得し、正解と解答を格納するMapを確保するメソッド
     */
    public TestSession() {
        this.startTime = System.currentTimeMillis();
        this.rawResult = new LinkedHashMap<>();
    }

    /**
     * 正解と解答を格納するメソッド
     * @param word 正解
     * @param answer ユーザーの解答
     */
    public void addResult(String word, String answer) {
        this.rawResult.put(word, answer);
    }

    /**
     * 所要時間を返すメソッド
     * @return 終了した時刻から始めた時刻を引き、所要時間を返す
     */
    public long getUsedTimeMillis() {
        return System.currentTimeMillis() - this.startTime;
    }

    /**
     * 正解と解答を格納したMapを返すメソッド
     * @return 正解と解答を格納したMap
     */
    public LinkedHashMap<String, String> getRawResult() {
        return this.rawResult;
    }
}
