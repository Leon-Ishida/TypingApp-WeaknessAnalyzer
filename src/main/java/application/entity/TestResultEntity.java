package application.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import application.model.MistakeDetail;
import application.model.TestResult;
import application.model.WordResult;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_result")
public class TestResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL)
    private List<WordResultEntity> wordResultEntities;

    private double wpm;

    private double accuracy;

    protected TestResultEntity() {}

    private TestResultEntity(LocalDateTime timestamp, List<WordResultEntity> wordResultEntities, double wpm, double accuracy) {
        this.timestamp = timestamp;
        this.wordResultEntities = wordResultEntities;
        this.wpm = wpm;
        this.accuracy = accuracy;
    }

    public TestResult toRecord() {
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();
        for (WordResultEntity wordResultEntity : wordResultEntities) {
            List<MistakeDetailEntity> mistakeDetailEntities = wordResultEntity.getMistakeDetailEntities();
            List<MistakeDetail> mistakeDetails = new ArrayList<>();
            for (MistakeDetailEntity mistakeDetailEntity : mistakeDetailEntities) {
                MistakeDetail mistakeDetail = new MistakeDetail(
                    mistakeDetailEntity.getMistakeType(),
                    mistakeDetailEntity.getExpected(),
                    mistakeDetailEntity.getActual(),
                    mistakeDetailEntity.getInsertion()
                );
                mistakeDetails.add(mistakeDetail);
            }
            WordResult wordResult = new WordResult(wordResultEntity.getAnswer(), mistakeDetails);
            results.put(wordResultEntity.getWord(), wordResult);
        }
        return new TestResult(timestamp, results, wpm, accuracy);
    }

    public static TestResultEntity fromRecord(TestResult testResult) {
        TestResultEntity testResultEntity = new TestResultEntity(
            testResult.timestamp(),
            new ArrayList<WordResultEntity>(),
            testResult.wpm(),
            testResult.accuracy()
        );
        for (Map.Entry<String, WordResult> entry : testResult.results().entrySet()) {
            WordResultEntity wordResultEntity = new WordResultEntity(
                testResultEntity,
                entry.getKey(),
                entry.getValue().answer(),
                new ArrayList<MistakeDetailEntity>()
            );
            for (MistakeDetail mistakeDetail : entry.getValue().mistakes()) {
                MistakeDetailEntity mistakeDetailEntity = new MistakeDetailEntity(
                    wordResultEntity,
                    mistakeDetail.mistakeType(),
                    mistakeDetail.expected(),
                    mistakeDetail.actual(),
                    mistakeDetail.insertion()
                );
                wordResultEntity.getMistakeDetailEntities().add(mistakeDetailEntity);
            }
            testResultEntity.getWordResultEntities().add(wordResultEntity);
        }
        return testResultEntity;
    }

    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getWpm() { return wpm; }
    public double getAccuracy() { return accuracy; }
    public List<WordResultEntity> getWordResultEntities() { return wordResultEntities; }
}
