package application.dto;

import java.util.List;

public record PracticeStartResponse(
    List<String> weaknessWords,
    List<String> frequentMistakeWords
) {}
