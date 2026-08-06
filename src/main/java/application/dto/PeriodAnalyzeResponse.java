package application.dto;

import java.util.List;

import application.model.AggregatedWordMistake;
import application.model.MistakeTopTrends;
import application.model.TestScoreTrend;

public record PeriodAnalyzeResponse(
    List<TestScoreTrend> scoreTrends,
    MistakeTopTrends mistakeTrends,
    List<AggregatedWordMistake> wordMistakes
) {}
