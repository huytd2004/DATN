package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.model.enums.JlptLevel;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps to the {@code dictionary_entries} table in PostgreSQL.
 */
@Entity
@Table(name = "dictionary_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "entry_type", nullable = false, columnDefinition = "entry_type_enum")
    private EntryType entryType;

    @Column(nullable = false)
    private String text;

    @Column(length = 500)
    private String reading;

    @Column(name = "meaning_vn", nullable = false, columnDefinition = "TEXT")
    private String meaningVn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jlpt_level", columnDefinition = "target_level_enum")
    private JlptLevel jlptLevel;

    @Column(name = "explanation_short", columnDefinition = "TEXT")
    private String explanationShort;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
