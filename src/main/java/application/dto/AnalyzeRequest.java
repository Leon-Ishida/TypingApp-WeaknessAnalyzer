package application.dto;

import java.util.LinkedHashMap;

public record AnalyzeRequest(
    LinkedHashMap<String, String> rawResult,
    long usedTimeMillis
) {}
