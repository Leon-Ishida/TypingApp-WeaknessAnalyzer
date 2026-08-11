package application.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import application.model.WordResult;

public record TestResultResponse(
    Long id,
    LocalDateTime timestamp,
    LinkedHashMap<String, WordResult> analyzedResult,
    double wpm,
    double accuracy
) {}
