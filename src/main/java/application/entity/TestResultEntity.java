package application.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import application.model.TestResult;
import application.model.WordResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Entity
public class TestResultEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String resultsJson;

    private double wpm;

    private double accuracy;

    protected TestResultEntity() {}

    private TestResultEntity(LocalDateTime timestamp, String resultsJson, double wpm, double accuracy) {
        this.timestamp = timestamp;
        this.resultsJson = resultsJson;
        this.wpm = wpm;
        this.accuracy = accuracy;
    }

    public TestResult toRecord() {
        LinkedHashMap<String, WordResult> results;
        try {
            results = MAPPER.readValue(
                resultsJson,
                new TypeReference<LinkedHashMap<String, WordResult>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("StringからMapに変換するのに失敗しました", e);
        }
        
        return new TestResult(
            timestamp,
            results,
            wpm,
            accuracy
        );
    }

    public static TestResultEntity fromRecord(TestResult testResult) {
        String resultsJson;
        try {
            resultsJson = MAPPER.writeValueAsString(testResult.results());
        } catch (Exception e) {
            throw new RuntimeException("MapからStringに変換するのに失敗しました", e);
        }
        return new TestResultEntity(
            testResult.timestamp(),
            resultsJson,
            testResult.wpm(),
            testResult.accuracy()
        );
    }

    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getResultsJson() { return resultsJson; }
    public double getWpm() { return wpm; }
    public double getAccuracy() { return accuracy; }
}
