package application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.dto.AnalyzeRequest;
import application.dto.AnalyzeResponse;
import application.dto.TestStartResponse;
import application.service.AnalyzeService;
import application.service.WordManager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/test")
public class TestController {
    private final WordManager wordManager;
    private final AnalyzeService analyzeService;

    public TestController(WordManager wordManager, AnalyzeService analyzeService) {
        this.wordManager = wordManager;
        this.analyzeService = analyzeService;
    }

    @GetMapping("/start")
    public TestStartResponse startTest() {
        return new TestStartResponse(
            wordManager.getTestWords(),
            System.currentTimeMillis()
        );
    }

    @PostMapping("/submit")
    public AnalyzeResponse submitTest(@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = analyzeService.submitResult(request);
        return response;
    }
    
    
}
