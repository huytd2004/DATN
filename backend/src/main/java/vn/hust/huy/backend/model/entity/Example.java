package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Example sentences for a dictionary entry.
 * Maps to the {@code examples} table.
 */
@Entity
@Table(name = "examples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private DictionaryEntry entry;

    @Column(name = "japanese_sentence", nullable = false, columnDefinition = "TEXT")
    private String japaneseSentence;

    @Column(name = "vietnamese_sentence", nullable = false, columnDefinition = "TEXT")
    private String vietnameseSentence;
}
