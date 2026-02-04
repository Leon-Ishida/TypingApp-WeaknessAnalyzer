package application.model;

import java.util.Map;

import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

/**
 * 各ミスの種類においてミスとなった文字と回数を格納するデータクラス
 * @param topSubstitutions 置換ミスにおいて元の文字と誤答した文字のペアと回数のMap
 * @param topTranspositions 交換ミスにおいて入れ替えた文字のペアと回数のMap
 * @param topDeletions 削除ミスにおいて消した文字と回数のMap
 * @param topInsertions 挿入ミスにおいて挿入した文字と回数のMap
 */

public record WeaknessAnalysisResult(
    Map<SubstitutionPair, Long> topSubstitutions,
    Map<TranspositionPair, Long> topTranspositions,
    Map<Character, Long> topDeletions,
    Map<InsertionPair, Long> topInsertions
) {}
