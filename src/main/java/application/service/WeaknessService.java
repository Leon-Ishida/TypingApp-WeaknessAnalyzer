package application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import application.dto.WeaknessResponse;
import application.model.TestResult;
import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

@Service
public class WeaknessService {
    private final AnalyzeService analyzeService;

    public WeaknessService(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    public WeaknessResponse analyzeWeakness() {
        TestResult lastResult = analyzeService.findLastResult();
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(List.of(lastResult));

        Map<SubstitutionPair, Long> topSub = analyzer.findTopSubstitutionMistakes(3);
        Map<TranspositionPair, Long> topTrans = analyzer.findTopTranspositionMistakes(3);
        Map<Character, Long> topDel = analyzer.findTopDeletionMistakes(3);
        Map<InsertionPair, Long> topIns = analyzer.findTopInsertionMistakes(3);

        return new WeaknessResponse(topSub, topTrans, topDel, topIns);
    }
}
