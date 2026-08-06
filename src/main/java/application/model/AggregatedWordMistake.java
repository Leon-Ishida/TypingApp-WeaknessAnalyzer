package application.model;

public record AggregatedWordMistake(
    String word,
    int totalMistakeCount,
    int attemptCount
) {}
