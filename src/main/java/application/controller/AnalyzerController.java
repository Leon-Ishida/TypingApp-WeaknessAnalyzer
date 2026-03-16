package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.dto.AnalyzeRequest;
import application.dto.AnalyzeResponse;
import application.service.AnalyzeService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api")
public class AnalyzerController {
    @Autowired
    private AnalyzeService analyzeService;

    @PostMapping("/analyze")
    public AnalyzeResponse analyzeResult(@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = analyzeService.analyzeResult(request);
        return response;
    }

    @PostMapping("/results")
    public AnalyzeResponse saveResult(@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = analyzeService.saveResult(request);
        return response;
    }

    @GetMapping("/results")
    public List<AnalyzeResponse> getAllResults() {
        return analyzeService.findAllResults();
    }

    @GetMapping("/results/{id}")
    public AnalyzeResponse getResultById(@PathVariable Long id) {
        return analyzeService.findResultById(id);
    }
}
