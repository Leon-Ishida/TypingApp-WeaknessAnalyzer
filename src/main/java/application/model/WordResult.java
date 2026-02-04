package application.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 各単語ごと結果を格納するデータクラス
 * @param answer ユーザーの入力した文字列
 * @param mistakes ミス詳細のリスト
 */

public record WordResult(
    String answer,
    List<MistakeDetail> mistakes
) {
    @JsonIgnore
    public boolean isCorrect() {
        return this.mistakes.isEmpty();
    }
}
