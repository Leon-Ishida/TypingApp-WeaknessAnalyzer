package application.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.model.MistakeDetail;
import application.model.MistakeType;

/**
 * 各文字でどのミスを犯したか判定するクラス
 */

public final class MistakeAnalyzer {
    //インスタンス化禁止
    private MistakeAnalyzer() {};

    /**
     * 制限ダメラウ・レーベンシュタイン距離のアルゴリズムを活用したミスの種類を判定するアルゴリズム
     * @param source 基準となる単語(今回は正解の文字列)
     * @param target 比較する単語(今回はユーザーが入力した文字列)
     * @return ミスの種類を犯した順に格納したリスト
     */
    public static List<MistakeDetail> analyzeMistakes(String source, String target) {
        /**
         * ダメラウ・レーベンシュタイン距離とは一方の単語を他方の単語に変換するのに必要な最小の操作回数である。
         * ここで、1回の操作とは1文字の挿入、削除、置換、あるいは隣り合う2文字の交換のことである
         * 
         * 制限ダメラウ・レーベンシュタイン距離を求めるアルゴリズム
         * 以下sourceの文字数をn、targetの文字数をmにする
         * (m+1)行(n+1)列の2次元配列においてi行目j列目は、sourceのj文字目までとtargetのi文字目までのダメラウ・レーベンシュタイン距離d(i,j)を表している
         * ダメラウ・レーベンシュタイン距離には以下の関係式が成り立つ
         * d(i,j) = min{
         * 0 if i == j == 1,
         * d(i-1,j) + 1 if i > 0, (1)
         * d(i,j - 1) + 1 if j > 0, (2)
         * d(i- 1,j -1) + cost if i,j > 0, (3)
         * d(i - 2, j - 2) + 1 if i,j > 1 and target[i] == source[j - 1] and target[i - 1] == source[j], (4)
         * }
         * ただし、costはtarget[i] == source[j]のとき0、他の場合1
         */

        int[][] d = new int[target.length() + 1][source.length() + 1];

         /*表の各地点におけるダメラウ・レーベンシュタイン距離を求める */
        for (int i = 0; i <= target.length(); i++) {
            for (int j = 0; j <= source.length(); j++) {
                //i == 0 または j == 0の時、その部分の距離はもう一方のindexと一致する
                if (i == 0) {
                    d[i][j] = j;
                } else if (j == 0) {
                    d[i][j] = i;
                } else {
                    //先ほどのcostの計算
                    int cost = (source.charAt(j - 1) == target.charAt(i - 1)) ? 0 : 1;

                    //関係式における(1)~(4)それぞれ計算
                    int subCost = d[i - 1][j - 1] + cost;
                    int transCost = Integer.MAX_VALUE;
                    int delCost = d[i][j - 1] + 1;
                    int insCost = d[i - 1][j] + 1;

                    if (i > 1 && j > 1 && target.charAt(i - 1) == source.charAt(j - 2) && target.charAt(i - 2) == source.charAt(j - 1)) {
                        transCost = d[i - 2][j - 2] + 1;
                    }

                    //4つのうち最小のものを探す
                    d[i][j] = Math.min(Math.min(subCost, transCost), Math.min(delCost, insCost));
                }
            }
        }

        /**
         * 表を逆探知してどのミスが起こっていたか調べる
         * d(i,j) == d(i - 1,j) + 1の時、挿入が起こっている
         * d(i,j) == d(i,j - 1) + 1の時、削除が起こっている
         * d(i,j) == d(i - 1,j - 1) + costの時、costが1なら置換が起こっており、0ならば一致している
         * d(i,j) == d(i - 2,j - 2) + 1の時、隣り合う2文字の交換が起こっている
         * これを用いて逆順でどのミスが起こったかを調べている
         */
        List<MistakeDetail> operations = new ArrayList<>();
        int i = target.length();
        int j = source.length();

        while (i > 0 || j > 0) {
            int currentCost = d[i][j];
            //costの計算
            int cost = (i > 0 && j > 0 && source.charAt(j - 1) == target.charAt(i- 1)) ? 0 : 1;

            //関係式における(1)~(4)を計算
            int subCost = (i > 0 && j > 0) ? d[i - 1][j - 1] + cost: Integer.MAX_VALUE;
            int transCost = Integer.MAX_VALUE;
            if (i > 1 && j > 1 && source.charAt(j - 1) == target.charAt(i - 2) && source.charAt(j - 2) == target.charAt(i - 1)) {
                transCost = d[i - 2][j - 2] + 1;
            }
            int delCost = (j > 0) ? d[i][j - 1] + 1 : Integer.MAX_VALUE;
            int insCost = (i > 0) ? d[i - 1][j] + 1 : Integer.MAX_VALUE;

            /**
             * ミスの種類、正解の1文字、誤った1文字、誤ったindexを判定する
             * もし経路が複数考えられるとき置換 > 交換 > 削除 > 挿入の順で優先する
             */
            if (currentCost == subCost) {
                if (cost == 1) {
                    operations.add(new MistakeDetail(MistakeType.SUBSTITUTION, source.charAt(j - 1), target.charAt(i - 1), null));
                }
                i--;
                j--;
            } else if (currentCost == transCost) {
                operations.add(new MistakeDetail(MistakeType.TRANSPOSITION, source.charAt(j - 2), target.charAt(i - 2), null));
                i -= 2;
                j -= 2;
            } else if (currentCost == delCost) {
                //削除の場合誤った1文字がないため\0を入れる
                operations.add(new MistakeDetail(MistakeType.DELETION, source.charAt(j - 1), null, null));
                j--;
            } else {
                //挿入の場合どの間に挿入したかが大事なためexpextedに前の文字、actualに後の文字を入れる
                Character beforeChar = (j > 0) ? source.charAt(j - 1) : null;
                Character afterChar = (j < source.length()) ? source.charAt(j) : null;
                operations.add(new MistakeDetail(MistakeType.INSERTION, beforeChar, afterChar, target.charAt(i - 1)));
                i--;
            }
        }

        //逆順で調べているため、ミスした順番にするため逆にする
        Collections.reverse(operations);
        return operations;
    }
}
