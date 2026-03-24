package application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TestStartResponse(
    List<String> words,
    LocalDateTime startedAt
) {}
