package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Intermediate table linking a word to its component kanji (or other relations).
 * Maps to the {@code entry_relations} table.
 */
@Entity
@Table(name = "entry_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** FK → dictionary_entries.id (the word that contains the kanji). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private DictionaryEntry source;

    /** FK → dictionary_entries.id (the kanji component / related entry). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private DictionaryEntry target;

    /**
     * Relation type — one of: kanji, radical, compound, synonym, antonym.
     * Stored as plain VARCHAR to avoid enum-mapping errors for future values.
     * See {@link vn.hust.huy.backend.model.enums.RelationType} for known values.
     */
    @Column(name = "relation_type", length = 50)
    private String relationType;

    /** Position of the kanji within the word (1-based). */
    @Column(name = "order_index")
    private Integer orderIndex;
}
