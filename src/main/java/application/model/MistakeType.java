package application.model;

/**
 * ミスの種類を限定する列挙クラス
 */

public enum MistakeType {
    SUBSTITUTION("置換"),   //置換
    TRANSPOSITION("交換"),   //交換
    DELETION("削除"),       //削除
    INSERTION("挿入");      //挿入

    private final String japaneseName;

    private MistakeType(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    public String getJapaneseName() {
        return this.japaneseName;
    }

    /**
     * ミスの日本語名からMistakeType定数を取得
     * @param japaneseName ミスの日本語名
     */
    public static MistakeType fromjapaneseName(String japaneseName) {
        for (MistakeType type : MistakeType.values()) {
            if (type.getJapaneseName().equals(japaneseName)) {
                return type;
            }
        }
        return null;
    }
}