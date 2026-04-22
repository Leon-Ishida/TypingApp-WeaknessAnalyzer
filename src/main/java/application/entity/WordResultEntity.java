package application.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "word_result")
public class WordResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TestResultEntity testResult;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String answer;

    @OneToMany(mappedBy = "wordResult", cascade = CascadeType.ALL)
    private List<MistakeDetailEntity> mistakeDetailEntities;

    protected WordResultEntity() {}

    public WordResultEntity(TestResultEntity testResult, String word, String answer, List<MistakeDetailEntity> mistakeDetailEntities) {
        this.testResult = testResult;
        this.word = word;
        this.answer = answer;
        this.mistakeDetailEntities = mistakeDetailEntities;
    }

    public Long getId() { return id; }
    public TestResultEntity getTestResult() {return testResult; }
    public String getWord() { return word; }
    public String getAnswer() { return answer; }
    public List<MistakeDetailEntity> getMistakeDetailEntities() { return mistakeDetailEntities; }
}
