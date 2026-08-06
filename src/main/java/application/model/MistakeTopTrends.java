package application.model;

import java.util.Map;

import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

public record MistakeTopTrends(
    Map<SubstitutionPair, Long> topSub,
    Map<TranspositionPair, Long> topTrans,
    Map<Character, Long> topDel,
    Map<InsertionPair, Long> topIns
) {}
