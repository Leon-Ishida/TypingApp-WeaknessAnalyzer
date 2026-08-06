package application.dto;

import java.time.LocalDate;

public record PeriodAnalyzeRequest(
    LocalDate beginDate,
    LocalDate endDate
) {}
