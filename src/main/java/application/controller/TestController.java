package application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.dto.TestResultRequest;
import application.dto.TestResultResponse;
import application.dto.TestStartResponse;
import application.service.AnalyzeService;
import application.service.WordManager;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/test")
public class TestController {
    private final WordManager wordManager;
    private final AnalyzeService analyzeService;

    public TestController(WordManager wordManager, AnalyzeService analyzeService) {
        this.wordManager = wordManager;
        this.analyzeService = analyzeService;
    }

    @GetMapping("/start")
    public TestStartResponse startTest(HttpSession session) {
        return new TestStartResponse(
            wordManager.getTestWords(),
            System.currentTimeMillis()
        );
    }

    @PostMapping("/results")
    public TestResultResponse submitTest(@RequestBody TestResultRequest request, HttpSession session) {
        TestResultResponse response = analyzeService.submitResult(request, session);
        return response;
    }
    
    @GetMapping("/results")
    public List<TestResultResponse> getAllResults() {
        return analyzeService.findAllResults();
    }

    @GetMapping("/results/{id}")
    public TestResultResponse getResultById(@PathVariable Long id) {
        return analyzeService.findResultById(id);
    }
}
