package application.dto;

import java.util.Map;

import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

public record WeaknessResponse(
    Map<SubstitutionPair, Long> topSub,
    Map<TranspositionPair, Long> topTrans,
    Map<Character, Long> topDel,
    Map<InsertionPair, Long> topIns
) {}
