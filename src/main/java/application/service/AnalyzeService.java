package application.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import application.dto.TestResultRequest;
import application.dto.TestResultResponse;
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

    public TestResultResponse analyzeResult(TestResultRequest request) {
        TestResult result = makeTestResult(request);
        return new TestResultResponse(
            null,
            result.timestamp(),
            result.results(),
            result.wpm(),
            result.accuracy()
        );
    }

    public TestResultResponse submitResult(TestResultRequest request) {
        TestResult result = makeTestResult(request);
        TestResultEntity entity = TestResultEntity.fromRecord(result);
        repository.save(entity);
        return translateFromEntity(entity);
    }

    public TestResult findLastResult() {
        TestResultEntity lastResultEntity = repository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new NoSuchElementException("テスト結果がありません"));
        return lastResultEntity.toRecord();
    }

    public List<TestResultResponse> findAllResults() {
        List<TestResultEntity> allResults = repository.findAll();
        return allResults.stream().map(this::translateFromEntity).toList();
    }

    public TestResultResponse findResultById(Long id) {
        TestResultEntity entity = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("一致するIdが存在しません"));
        return translateFromEntity(entity);
    }

    private TestResult makeTestResult(TestResultRequest request) {
        return analyzer.analyze(request.rawResult(), request.usedTimeMillis());
    }

    private TestResultResponse translateFromEntity(TestResultEntity entity) {
        return new TestResultResponse(
            entity.getId(),
            entity.getTimestamp(),
            entity.toRecord().results(),
            entity.getWpm(),
            entity.getAccuracy()
        );
    }
}
