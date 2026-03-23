package application.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import application.dto.AnalyzeRequest;
import application.dto.AnalyzeResponse;
import application.entity.TestResultEntity;
import application.model.TestResult;
import application.repository.TestResultRepository;
import application.typingtest.TypingAnalyzer;

@Service
public class AnalyzeService {
    private final TypingAnalyzer analyzer;
    private final TestResultRepository repository;

    public AnalyzeService(TypingAnalyzer analyzer, TestResultRepository repository) {
        this.analyzer = analyzer;
        this.repository = repository;
    }

    public AnalyzeResponse analyzeResult(AnalyzeRequest request) {
        TestResult result = makeTestResult(request);
        return new AnalyzeResponse(
            null,
            result.timestamp(),
            result.results(),
            result.wpm(),
            result.accuracy()
        );
    }

    public AnalyzeResponse saveResult(AnalyzeRequest request) {
        TestResult result = makeTestResult(request);
        TestResultEntity entity = TestResultEntity.fromRecord(result);
        repository.save(entity);
        return translateFromEntity(entity);
    }

    public List<AnalyzeResponse> findAllResults() {
        List<TestResultEntity> allResults = repository.findAll();
        return allResults.stream().map(this::translateFromEntity).toList();
    }

    public AnalyzeResponse findResultById(Long id) {
        TestResultEntity entity = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("一致するIdが存在しません"));
        return translateFromEntity(entity);
    }

    private TestResult makeTestResult(AnalyzeRequest request) {
        return analyzer.analyze(request.rawResult(), request.usedTimeMillis());
    }

    private AnalyzeResponse translateFromEntity(TestResultEntity entity) {
        return new AnalyzeResponse(
            entity.getId(),
            entity.getTimestamp(),
            entity.toRecord().results(),
            entity.getWpm(),
            entity.getAccuracy()
        );
    }
}
