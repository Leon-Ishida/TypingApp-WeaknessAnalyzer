package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.dto.AnalyzeRequest;
import application.dto.AnalyzeResponse;
import application.service.AnalyzeService;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AnalyzerController {
    private final AnalyzeService analyzeService;

    public AnalyzerController(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyzeResult(@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = analyzeService.analyzeResult(request);
        return response;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> invalidIdException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
