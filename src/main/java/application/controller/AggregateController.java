package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.dto.PeriodAnalyzeRequest;
import application.dto.PeriodAnalyzeResponse;
import application.service.AggregateService;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AggregateController {
    private final AggregateService aggregateService;

    public AggregateController(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @PostMapping("/aggregate")
    public PeriodAnalyzeResponse aggregateResult(@RequestBody PeriodAnalyzeRequest request) {
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(request);
        return response;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> invalidIdException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
