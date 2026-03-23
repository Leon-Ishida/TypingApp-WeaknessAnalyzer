package application.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import application.model.WordResult;

public record AnalyzeResponse(
    Long id,
    LocalDateTime timestamp,
    LinkedHashMap<String, WordResult> results,
    double wpm,
    double accuracy
) {}
