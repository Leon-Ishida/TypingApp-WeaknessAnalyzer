package application.model;

import java.time.LocalDateTime;

public record TestScoreTrend(
    LocalDateTime timestamp,
    double wpm,
    double accuracy
) {}
