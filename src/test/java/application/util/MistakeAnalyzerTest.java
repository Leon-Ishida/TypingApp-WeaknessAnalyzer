package application.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import application.model.MistakeDetail;
import application.model.MistakeType;

class MistakeAnalyzerTest {
    static Stream<Arguments> provideTheOtherMistakeCases() {
        return Stream.of(
            //完全一致
            Arguments.of("hello", "hello", 0, null, '\0', '\0'),
            
            //置換 (e -> i)
            Arguments.of("hello", "hillo", 1, MistakeType.SUBSTITUTION, 'e', 'i'),

            //交換 (el -> le)
            Arguments.of("hello", "hlelo", 1, MistakeType.TRANSPOSITION, 'e', 'l'),
            
            //削除 (l が抜ける)
            Arguments.of("hello", "helo", 1, MistakeType.DELETION, 'l', '\0'),
            
            //空文字同士
            Arguments.of("", "", 0, null, '\0', '\0')
        );
    }

    static Stream<Arguments> provideInsertionMistakeCases() {
        return Stream.of(
            //挿入 (w が先頭に入る)
            Arguments.of("hello", "whello", 1, '\0', 'h', 'w'),

            //挿入 (w が中間に入る)
            Arguments.of("hello", "hewllo", 1, 'e', 'l', 'w'),

            //挿入 (w が末尾に入る)
            Arguments.of("hello", "hellow", 1, 'o', '\0', 'w')
        );
    }

    @ParameterizedTest
    @DisplayName("挿入ミス以外のミスの検証および完全一致と空文字の検証")
    @MethodSource("provideTheOtherMistakeCases")
    void testTheOtherMistakes(String source, String target, int expectedSize,
        MistakeType expectedType, char expectedChar, char actualChar) {
        
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes(source, target);

        // ミスの数が合っているか確認
        assertEquals(expectedSize, mistakes.size(), 
            () -> String.format("入力: '%s' -> '%s' のミス数が期待と異なります", source, target));

        // ミスがある場合のみ詳細をチェック
        if (expectedSize > 0) {
            for (int i = 0; i < expectedSize; i++) {
                MistakeDetail mistake = mistakes.get(i);
                int count = i; 

                assertAll("ミスがある場合のミスの種類とExpected,Actual検証",
                    () -> assertEquals(expectedType, mistake.mistakeType(), 
                        () -> String.format("'%d' 番目のミスの種類が違います", count + 1)
                    ),
                    () -> assertEquals(expectedChar, mistake.expected(),
                        () -> String.format("'%d' 番目のExpected文字が違います", count + 1)
                    ),
                    () -> assertEquals(actualChar, mistake.actual(),
                        () -> String.format("'%d' 番目のActual文字が違います", count + 1)
                    ),
                    () -> assertEquals('\0', mistake.insertion(),
                        () -> String.format("'%d' 番目のInsertion文字が違います", count + 1)
                    )
                );
            }
        }
    }

    @ParameterizedTest
    @DisplayName("挿入ミスの検証")
    @MethodSource("provideInsertionMistakeCases")
    void testInsertionMistakes(String source, String target, int expectedSize,
            char expectedBeforeChar, char expectedAfterChar, char expectedInsertionChar) {
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes(source, target);

        //挿入ミスのテスト項目はすべて1つだけミスした形式にしているため、mistakesから先頭のみを取る
        MistakeDetail mistake = mistakes.get(0);

        assertAll("挿入ミスの検証",
            () -> assertEquals(expectedSize, mistakes.size(),
                    () -> String.format("入力: '%s -> '%s' のミス数が期待と異なります", source, target)
                ),
            () -> assertEquals(expectedBeforeChar, mistake.expected(), "挿入文字の1文字前が違います"),
            () -> assertEquals(expectedAfterChar, mistake.actual(), "挿入文字の1文字後が違います"),
            () -> assertEquals(expectedInsertionChar, mistake.insertion(), "挿入文字が違います")
        );
    }
    
    @Test
    @DisplayName("複合ミスの検証")
    void testComplexMistakes() {
        // "correct" -> "curret" (o->u 置換, c削除)
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes("correct", "curret");
        
        assertAll("置換と削除の複合ミスの検証",
            () -> assertEquals(2, mistakes.size(), "ミスは2つであるべき"),
            () -> assertEquals(MistakeType.SUBSTITUTION, mistakes.get(0).mistakeType(), "1つ目のミスは置換ミスであるべき"),
            () -> assertEquals(MistakeType.DELETION, mistakes.get(1).mistakeType(), "2つ目のミスは削除ミスであるべき")
        );


        // "abcde" -> "bacdef" (a->d 交換, f挿入)
        List<MistakeDetail> mistakes2 = MistakeAnalyzer.analyzeMistakes("abcde", "bacdef");
        
        assertAll("交換と挿入の複合ミスの検証",
            () -> assertEquals(2, mistakes2.size(), "ミスは2つであるべき"),
            () -> assertEquals(MistakeType.TRANSPOSITION, mistakes2.get(0).mistakeType(), "1つ目のミスは交換であるべき"),
            () -> assertEquals(MistakeType.INSERTION, mistakes2.get(1).mistakeType(), "2つ目のミスは挿入ミスであるべき")
        );
    }

    @Test
    @DisplayName("同じ文字が連続する単語の検証")
    void testSameChar() {
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes("book", "bok");

        assertAll("同じ文字が連続する単語の検証",
            () -> assertEquals(1, mistakes.size(), "ミスは1つであるべき"),
            () -> assertEquals(MistakeType.DELETION, mistakes.get(0).mistakeType(), "ミスは削除であるべき")
        );
    }

    @Test
    @DisplayName("sourceが空文字の時の検証")
    void testEmptySource() {
        String target = "abc";
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes("", target);

        assertEquals(3, mistakes.size(), "ミスは3つであるべき");
        
        for (int i = 0; i < mistakes.size(); i++) {
            MistakeDetail mistake = mistakes.get(i);
            int count = i;

            assertAll("test",
                () -> assertEquals(MistakeType.INSERTION, mistake.mistakeType(),
                    () -> String.format("'%d' 番目のミスの種類が違います", count + 1)
                ),
                () -> assertEquals('\0', mistake.expected(),
                    () -> String.format("'%d' 番目の挿入文字の1文字前が違います", count + 1)    
                ),
                () -> assertEquals('\0', mistake.actual(),
                    () -> String.format("'%d' 番目の挿入文字の1文字後が違います", count + 1)
                ),
                () -> assertEquals(target.charAt(count), mistake.insertion(),
                    () -> String.format("'%d' 番目の挿入文字が違います", count + 1)
                )
            );
        }
    }

    @Test
    @DisplayName("targetが空文字の時の検証")
    void testEmptyTarget() {
        String source = "abc";
        List<MistakeDetail> mistakes = MistakeAnalyzer.analyzeMistakes(source, "");

        assertEquals(3, mistakes.size(), "ミスは3つであるべき");

        for (int i = 0; i < mistakes.size(); i++) {
            MistakeDetail mistake = mistakes.get(i);
            int count = i;

            assertAll("test",
                () -> assertEquals(MistakeType.DELETION, mistake.mistakeType(),
                    () -> String.format("'%d' 番目のミスの種類が違います", count + 1)
                ),
                () -> assertEquals(source.charAt(count), mistake.expected(),
                    () -> String.format("'%d' 番目の挿入文字の1文字前が違います", count + 1)    
                ),
                () -> assertEquals('\0', mistake.actual(),
                    () -> String.format("'%d' 番目の挿入文字の1文字後が違います", count + 1)
                ),
                () -> assertEquals('\0', mistake.insertion(),
                    () -> String.format("'%d' 番目の挿入文字が違います", count + 1)
                )
            );
        }
    }
}