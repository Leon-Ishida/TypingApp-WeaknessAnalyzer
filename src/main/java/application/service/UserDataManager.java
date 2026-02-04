package application.service;

import java.util.Collections;
import java.util.List;

import application.model.FilteredResult;
import application.model.TestResult;
import application.result.ResultFilter;
import application.result.ResultRepository;
import application.result.FileResultRepository;

/**
 * 結果を追加しフィルターにかけ、渡すクラス
 * 結果表示は2つのルートがあるため、同一のインスタンスを使わせる
 */

public class UserDataManager {
    private static final UserDataManager instance = new UserDataManager();

    private final ResultRepository repository;
    private List<TestResult> allResults;
    private FilteredResult filteredResult;

    //初めてインスタンス化されるとき全結果を呼びフィルターを掛ける
    private UserDataManager() {
        this.repository = new FileResultRepository();
        loadAndFilterAllResults();
    };

    //同一のインスタンスを取得させる
    public static UserDataManager getInstance() {
        return instance;
    }

    /**
     * 結果が保存されているファイルから全結果を取得し、フィルタをかける
     */
    private void loadAndFilterAllResults() {
        FileResultRepository manager = new FileResultRepository();
        this.allResults = manager.findAll();

        if (this.allResults != null && !this.allResults.isEmpty()) {
            this.filteredResult = ResultFilter.filterAll(allResults);
        } else {
            //ガード節
            this.filteredResult = new FilteredResult(
                null, 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            );
        }
    }

    /**
     * 全結果を返すメソッド
     * @return 今までの全結果が入ったリスト
     */
    public List<TestResult> getAllResults() {
        return this.allResults;
    }

    /**
     * フィルターを掛けた後のリストが格納されたFilteredResultオブジェクトを返すメソッド
     * @return 各期間ごとに分類されたリストが格納されたFilteredResultオブジェクト
     */
    public FilteredResult getFilteredResult() {
        return this.filteredResult;
    }

    /**
     * テスト終了時に呼ばれ、データの再読み込みと再フィルタリングをするメソッド
     */
    public void refreshData() {
        loadAndFilterAllResults();
    }
}
