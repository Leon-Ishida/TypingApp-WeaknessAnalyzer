package application.dto;

import java.util.LinkedHashMap;

public record TestResultRequest(
    LinkedHashMap<String, String> rawResult,
    long usedTimeMillis
) {}
