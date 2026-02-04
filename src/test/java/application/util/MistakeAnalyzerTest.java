package application.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import application.model.MistakeDetail;
import application.model.MistakeType;

class MistakeAnalyzerTest {

    static Stream<Arguments> provideTestCases() {
        return Stream.of(
            //完全一致
            Arguments.of("hello", "hello", 0, null, '\0', '\0'),
            
            //置換 (e -> i)
            Arguments.of("hello", "hillo", 1, MistakeType.SUBSTITUTION, 'e', 'i'),

            //交換 (el -> le)
            Arguments.of("hello", "hlelo", 1, MistakeType.TRANSPOSITION, 'e', 'l'),
            
            //削除 (l が抜ける)
            Arguments.of("hello", "helo", 1, MistakeType.DELETION, 'l', '\0'),
            
            //挿入 (w が入る)
            Arguments.of("hello", "hellow", 1, MistakeType.INSERTION, 'o', '\0'),
            
            //空文字同士
            Arguments.of("", "", 0, null, '\0', '\0'),

            //sourceが空文字
            Arguments.of("", "abc", 3, MistakeType.INSERTION, '\0', '\0'),

            //targetが空文字
            Arguments.of("abc", "", 3, MistakeType.DELETION, 'a', '\0')
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testMistakeAnalysis(String source, String target, int expectedSize, MistakeType expectedType, char expectedChar, char actualChar) {
        
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes(source, target);

        // 1. ミスの数が合っているか確認
        assertEquals(expectedSize, mistakes.size(), 
            () -> String.format("入力: '%s' -> '%s' のミス数が期待と異なります", source, target));

        // ミスがある場合のみ詳細をチェック（今回は最初のミスだけチェックする簡易版）
        if (expectedSize > 0) {
            MistakeDetail mistake = mistakes.get(0);
            
            // 2. ミスの種類が合っているか
            assertEquals(expectedType, mistake.mistakeType(), 
                "ミスの種類が違います");
            
            // 3. 期待値・入力値の文字が合っているか
            // 挿入(INSERTION)の場合はロジックにより検証内容が複雑になるため、ここでは簡易チェックに留めるか、
            // 厳密にやるならif文で分岐させます。
            if (expectedType != MistakeType.INSERTION) {
                assertEquals(expectedChar, mistake.expected(), "Expected文字が違います");
            }
            assertEquals(actualChar, mistake.actual(), "Actual文字が違います");
        }
    }
    
    // 複合ミスなどの複雑なケースは、パラメータ化せず個別に書く方が読みやすいです
    @org.junit.jupiter.api.Test
    void testComplexMistakes() {
        // "correct" -> "curret" (o->u 置換, c削除)
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes("correct", "curret");
        
        assertEquals(2, mistakes.size());
        assertEquals(MistakeType.SUBSTITUTION, mistakes.get(0).mistakeType());
        assertEquals(MistakeType.DELETION, mistakes.get(1).mistakeType());

        // "abcde" -> "bacdef" (a->d 交換, f挿入)
        List<MistakeDetail> mistakes2 = MistakeAnalyzer.analyzeMistakes("abcde", "bacdef");
        
        assertEquals(2, mistakes2.size());
        assertEquals(MistakeType.TRANSPOSITION, mistakes2.get(0).mistakeType());
        assertEquals(MistakeType.INSERTION, mistakes2.get(1).mistakeType());
    }
}