package application.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.model.AppConfig;

/**
 * 単語を格納しているjsonファイルから単語リストを取得するクラス
 */

@Service
public class WordManager {
    private List<String> words;

    public WordManager() {
        loadWord();
    }

    /**
     * jsonファイルから単語リストを作るメソッド
     */
    private void loadWord() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<String>> typeReference = new TypeReference<>() {};
        InputStream inputStream = WordManager.class.getResourceAsStream("/application/wordlist.json");

        if (inputStream == null) {
            System.err.println("エラー:辞書ファイル /application/wordlist.json が見つかりません");
            words = List.of("file not found");
            return;
        }

        try {
            words = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            System.err.println("単語ファイルの読み込み、またはJSONの解析に失敗しました");
            e.printStackTrace();
            words = List.of("error");
        }
    }

    /**
     * 単語リストからランダムに単語を取得するメソッド
     * @return ランダムに取得した単語
     */
    public String getRandomWord() {
        if (words == null || words.isEmpty()) {
            return "no words";
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    /**
     * 単語リストの順番をランダムにするメソッド
     */
    public void shuffleWords() {
        if (words != null) {
            Collections.shuffle(words);
        }
    }

    /**
     * 取得した単語を格納したListを渡すメソッド
     * @return 単語を格納したList
     */
    public List<String> getWords() {
        return this.words;
    }

    /**
     * 単語を自分で設定するメソッド
     * @param words 新たに設定したい単語を含んだList
     */
    public void setWords(List<String> words) {
        this.words = words;
    }

    /**
     * テスト問題の単語リストを渡すメソッド
     * wordsをシャッフルした後に問題数を制限してから返す
     * @return テスト問題数に制限された単語リスト
     */
    public List<String> getTestWords() {
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, AppConfig.TEST_NUMBERS_OF_QUESTIONS);
    }
}
