package application.model;

/**
 * 期間の種類を限定する列挙クラス
 */

public enum Period {
    LAST("前回"),
    TODAY("今日"),
    THISWEEK("今週"),
    THISMONTH("今月"),
    ALLTIME("全期間");

    private final String displayName;

    private Period(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
