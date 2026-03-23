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

@Entity
public class MistakeDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private WordResultEntity wordResult;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MistakeType mistakeType;
    
    private char expected;

    private char actual;

    private char insertion;

    protected MistakeDetailEntity() {}

    public MistakeDetailEntity(WordResultEntity wordResult, MistakeType mistakeType, char expected, char actual, char insertion) {
        this.wordResult = wordResult;
        this.mistakeType = mistakeType;
        this.expected = expected;
        this.actual = actual;
        this.insertion = insertion;
    }

    public Long getId() { return id; }
    public WordResultEntity getWordResult() { return wordResult; }
    public MistakeType getMistakeType() { return mistakeType; }
    public char getExpected() { return expected; }
    public char getActual() { return actual; }
    public char getInsertion() { return insertion; }
}
