package application.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.dto.PracticeStartResponse;
import application.dto.WeaknessResponse;
import application.model.PracticeWords;
import application.model.TestResult;
import application.service.AnalyzeService;
import application.service.PracticeService;
import application.service.WeaknessAnalyzer;
import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;



@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    private final AnalyzeService analyzeService;
    private final PracticeService practiceService;

    public PracticeController(AnalyzeService analyzeService, PracticeService practiceService) {
        this.analyzeService = analyzeService;
        this.practiceService = practiceService;
    }
    
    @GetMapping("/weakness")
    public WeaknessResponse getWeakness() {
        TestResult lastResult = analyzeService.findLastResult();
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(List.of(lastResult));

        Map<SubstitutionPair, Long> topSub = analyzer.findTopSubstitutionMistakes(3);
        Map<TranspositionPair, Long> topTrans = analyzer.findTopTranspositionMistakes(3);
        Map<Character, Long> topDel = analyzer.findTopDeletionMistakes(3);
        Map<InsertionPair, Long> topIns = analyzer.findTopInsertionMistakes(3);

        return new WeaknessResponse(topSub, topTrans, topDel, topIns);
    }

    @GetMapping("/start")
    public PracticeStartResponse startPractice() {
        PracticeWords practiceWords = practiceService.generatePracticeWords(List.of(analyzeService.findLastResult()));
        return new PracticeStartResponse(
            practiceWords.weaknessWords(),
            practiceWords.frequentMistakeWords()
        );
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
