package application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import application.dto.PeriodAnalyzeRequest;
import application.dto.PeriodAnalyzeResponse;
import application.entity.TestResultEntity;
import application.model.AppConfig;
import application.model.MistakeTopTrends;
import application.model.TestResult;
import application.model.TestScoreTrend;
import application.repository.TestResultRepository;
import application.service.WeaknessAnalyzer.InsertionPair;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

@Service
public class AggregateService {
    private final TestResultRepository repository;

    public AggregateService(TestResultRepository repository) {
        this.repository = repository;
    }

    public PeriodAnalyzeResponse makeAggregatedInfo(PeriodAnalyzeRequest request) {
        LocalDateTime startDateTime;
        LocalDateTime lastDateTime;
        List<TestResultEntity> selectedRecords;
        if (request.startDate() != null) {
        // 選択した期間の記録のみを抽出する
            startDateTime = request.startDate().atStartOfDay();
            lastDateTime = request.lastDate().plusDays(1).atStartOfDay();
            selectedRecords = repository.findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestamp(startDateTime, lastDateTime);
        } else {
            // 指定がない場合全期間の記録を抽出する
            selectedRecords = repository.findAllByOrderByTimestamp();
        }

        // 選択した期間のwpmおよび正答率の推移をListにまとめる
        // 選択した期間の記録が30件より多いなら日にちごとの平均の推移を返す
        List<TestScoreTrend> scoreTrends = new ArrayList<>();
        if (selectedRecords.size() > AppConfig.MAX_NOT_AVERAGE_OF_TRANSITION) {
            Iterator<TestResultEntity> resultIte = selectedRecords.iterator();
            TestResultEntity firstRecord = resultIte.next();
            LocalDate baseDate = firstRecord.getTimestamp().toLocalDate();
            double sumWpm = firstRecord.getWpm();
            double sumAccuracy = firstRecord.getAccuracy();
            int countOfSameDay = 1;

            for (;!resultIte.hasNext();) {
                TestResultEntity record = resultIte.next();
                if (record.getTimestamp().toLocalDate().isEqual(baseDate)) {
                    sumWpm += record.getWpm();
                    sumAccuracy += record.getAccuracy();
                    countOfSameDay++;
                } else {
                    scoreTrends.add(new TestScoreTrend(
                            baseDate.atStartOfDay(),
                            sumWpm / countOfSameDay,
                            sumAccuracy / countOfSameDay
                        ));
                    baseDate = record.getTimestamp().toLocalDate();
                    sumWpm = record.getWpm();
                    sumAccuracy = record.getAccuracy();
                    countOfSameDay = 1;
                }
            }
        } else {
            for (TestResultEntity record : selectedRecords) {
                scoreTrends.add(new TestScoreTrend(
                    record.getTimestamp(),
                    record.getWpm(),
                    record.getAccuracy()
                ));
            }
        }

        // 各ミスの種類のうちTop3(`application.model.AppConfig`で指定)のミスをそれぞれまとめる
        List<TestResult> results = new ArrayList<>();
        for (TestResultEntity entity : selectedRecords) {
            results.add(entity.toRecord());
        }
        WeaknessAnalyzer analyzer = new WeaknessAnalyzer(results);

        Map<SubstitutionPair, Long> topSub = analyzer.findTopSubstitutionMistakes(AppConfig.MAX_LIMIT_FOR_EACH_TYPE_OF_ERR);
        Map<TranspositionPair, Long> topTrans = analyzer.findTopTranspositionMistakes(AppConfig.MAX_LIMIT_FOR_EACH_TYPE_OF_ERR);
        Map<Character, Long> topDel = analyzer.findTopDeletionMistakes(AppConfig.MAX_LIMIT_FOR_EACH_TYPE_OF_ERR);
        Map<InsertionPair, Long> topIns = analyzer.findTopInsertionMistakes(AppConfig.MAX_LIMIT_FOR_EACH_TYPE_OF_ERR);

        MistakeTopTrends mistakeTrends = new MistakeTopTrends(topSub, topTrans, topDel, topIns);

        return new PeriodAnalyzeResponse(scoreTrends, mistakeTrends);
    }
}
