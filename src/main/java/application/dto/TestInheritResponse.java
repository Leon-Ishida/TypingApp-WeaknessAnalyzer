package application.dto;

import java.time.LocalDateTime;

public record TestInheritResponse(
    Long id,
    LocalDateTime timestamp,
    double wpm,
    double accuracy
) {}
