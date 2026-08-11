package application.dto;

import java.time.LocalDate;

import application.model.PracticeMode;
import jakarta.validation.constraints.NotNull;

public record PracticeGenerateRequest(
    LocalDate startDate,
    LocalDate lastDate,
    
    @NotNull
    PracticeMode mode
) {}
