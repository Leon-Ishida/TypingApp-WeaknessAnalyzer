package application.entity;

import application.model.MistakeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mistake_detail")
public class MistakeDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private WordResultEntity wordResult;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MistakeType mistakeType;
    
    private Character expected;

    private Character actual;

    private Character insertion;

    protected MistakeDetailEntity() {}

    public MistakeDetailEntity(WordResultEntity wordResult, MistakeType mistakeType, Character expected, Character actual, Character insertion) {
        this.wordResult = wordResult;
        this.mistakeType = mistakeType;
        this.expected = expected;
        this.actual = actual;
        this.insertion = insertion;
    }

    public Long getId() { return id; }
    public WordResultEntity getWordResult() { return wordResult; }
    public MistakeType getMistakeType() { return mistakeType; }
    public Character getExpected() { return expected; }
    public Character getActual() { return actual; }
    public Character getInsertion() { return insertion; }
}
