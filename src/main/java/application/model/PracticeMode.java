package application.model;

/**
 * 練習のモードを限定する列挙クラス
 */

public enum PracticeMode {
    WEAKNESS("弱点克服"),
    FREQUENT_MISTAKE("頻出ミス単語"),
    ENDLESS("エンドレス");

    private final String displayName;

    PracticeMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
