package application.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.dto.PracticeStartResponse;
import application.dto.WeaknessResponse;
import application.model.PracticeWords;
import application.service.AnalyzeService;
import application.service.PracticeService;
import application.service.WeaknessService;



@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    private final AnalyzeService analyzeService;
    private final PracticeService practiceService;
    private final WeaknessService weaknessService;

    public PracticeController(
        AnalyzeService analyzeService,
        PracticeService practiceService,
        WeaknessService weaknessService
    ) {
        this.analyzeService = analyzeService;
        this.practiceService = practiceService;
        this.weaknessService = weaknessService;
    }
    
    @GetMapping("/weakness")
    public WeaknessResponse getWeakness() {
        return weaknessService.analyzeWeakness();
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
