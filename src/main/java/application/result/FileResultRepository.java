package application.result;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import application.model.TestResult;

import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * 結果を保存するjsonファイル(typingApp_history.json)から結果を取得および保存をするクラス
 */

public class FileResultRepository implements ResultRepository {
    private ObjectMapper mapper;
    final private String SAVE_FILE_PATH = System.getProperty("user.home") + File.separator + "typingApp_history.json"; //結果の保存先パス

    public FileResultRepository() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 新しいテスト結果をjsonファイルに追記するメソッド
     * @param newResult 新しいテスト結果
     */
    @Override
    public void save(TestResult newResult) {
        try {
            File saveFile = new File(SAVE_FILE_PATH);
            ArrayList<TestResult> history;

            //既にセーブされたデータがある場合読み込む
            if(saveFile.exists() && saveFile.length() > 0) {
                history = this.mapper.readValue(saveFile, new TypeReference<ArrayList<TestResult>>() {});
            } else {
                history = new ArrayList<>();
            }

            //ガード節
            if (newResult != null) {
                history.add(newResult);
            }
            this.mapper.writeValue(saveFile, history);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイルから全結果のリストを取得するメソッド
     * @return 全結果のリスト
     * ファイルが存在しない、または空の場合には空のリストを返す
     */
    @Override
    public List<TestResult> findAll() {
        try {
            File saveFile = new File(SAVE_FILE_PATH);
            if (saveFile.exists() && saveFile.length() > 0) {
                return this.mapper.readValue(saveFile, new TypeReference<List<TestResult>>() {});
            } else {
                System.out.println("履歴無し");
                return Collections.emptyList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
