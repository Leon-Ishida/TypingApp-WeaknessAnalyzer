package application.model;

/**
 * ミスと位置について格納するデータクラス
 * @param mistakeType ミスの種類
 * @param expected 正解の文字(挿入の場合挿入した文字の1文字前の文字)
 * @param actual 誤って入力した文字(挿入の場合挿入した1文字後の文字)
 */

public record MistakeDetail(
    MistakeType mistakeType,
    char expected,
    char actual,
    char insertion
) {}
