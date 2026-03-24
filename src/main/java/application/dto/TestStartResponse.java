package application.dto;

import java.util.List;

public record TestStartResponse(
    List<String> words,
    long startedAt
) {}
