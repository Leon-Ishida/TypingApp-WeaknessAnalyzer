package application.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;

    public Memo() {}

    public Memo(String text) { this.text = text; }

    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
