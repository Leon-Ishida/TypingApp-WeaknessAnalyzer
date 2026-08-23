package application.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.dto.PracticeGenerateRequest;
import application.service.PracticeService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/practice")
public class PracticeAPIController {
    private final PracticeService practiceService;

    public PracticeAPIController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @Valid
    @PostMapping("/start")
    public List<String> startPractice(@RequestBody PracticeGenerateRequest request, Authentication authentication) {
        return practiceService.generatePracticeWords(request, authentication);
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
