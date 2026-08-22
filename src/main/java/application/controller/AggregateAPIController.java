package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.dto.PeriodAnalyzeRequest;
import application.dto.PeriodAnalyzeResponse;
import application.service.AggregateService;
import jakarta.servlet.http.HttpSession;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AggregateAPIController {
    private final AggregateService aggregateService;

    public AggregateAPIController(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @PostMapping("/aggregate")
    public PeriodAnalyzeResponse aggregateResult(@RequestBody PeriodAnalyzeRequest request, Authentication authentication, HttpSession session) {
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(request, authentication, session);
        return response;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> invalidIdException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
