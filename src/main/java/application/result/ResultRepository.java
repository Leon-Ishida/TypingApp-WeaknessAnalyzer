package application.result;

import java.util.List;
import application.model.TestResult;

/**
 * 結果データの保存・取得に関するインターフェース
 */
public interface ResultRepository {
    //データを1件保存する
    void save(TestResult result);

    //全データを取得する
    List<TestResult> findAll();
}
