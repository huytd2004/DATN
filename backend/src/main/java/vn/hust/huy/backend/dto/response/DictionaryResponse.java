package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.model.entity.EntryRelation;
import vn.hust.huy.backend.model.entity.Example;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.model.enums.JlptLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read model returned to the client for a single dictionary entry.
 * Includes nested examples and entry relations.
 */
@Getter
@Builder
public class DictionaryResponse {

    private UUID id;
    private String text;
    private EntryType entryType;
    private String reading;
    private String meaningVn;
    private JlptLevel jlptLevel;
    private String explanationShort;
    private Instant createdAt;

    /** Ví dụ câu từ bảng {@code examples}. */
    private List<ExampleDto> examples;

    /** Quan hệ từ bảng {@code entry_relations} (kanji_component, compound, synonym, radical...). */
    private List<RelationDto> relations;

    // ── Nested DTOs ────────────────────────────────────────────────────────────

    @Getter
    @Builder
    public static class ExampleDto {
        private UUID id;
        private String japaneseSentence;
        private String vietnameseSentence;

        public static ExampleDto from(Example e) {
            return ExampleDto.builder()
                    .id(e.getId())
                    .japaneseSentence(e.getJapaneseSentence())
                    .vietnameseSentence(e.getVietnameseSentence())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class RelationDto {
        private UUID id;
        private String text;
        private String reading;
        private String meaningVn;
        private String relationType;
        private Integer orderIndex;

        public static RelationDto from(EntryRelation r) {
            DictionaryEntry target = r.getTarget();
            return RelationDto.builder()
                    .id(r.getId())
                    .text(target.getText())
                    .reading(target.getReading())
                    .meaningVn(target.getMeaningVn())
                    .relationType(r.getRelationType())
                    .orderIndex(r.getOrderIndex())
                    .build();
        }
    }

    // ── Factory ────────────────────────────────────────────────────────────────

    /**
     * Creates a response from entity only (no enrichment — used in list results
     * to avoid N+1 queries; examples/relations will be null).
     */
    public static DictionaryResponse fromEntity(DictionaryEntry e) {
        return DictionaryResponse.builder()
                .id(e.getId())
                .text(e.getText())
                .entryType(e.getEntryType())
                .reading(e.getReading())
                .meaningVn(e.getMeaningVn())
                .jlptLevel(e.getJlptLevel())
                .explanationShort(e.getExplanationShort())
                .createdAt(e.getCreatedAt())
                .build();
    }

    /**
     * Creates a fully enriched response including examples and relations.
     */
    public static DictionaryResponse fromEntity(DictionaryEntry e,
                                                List<Example> examples,
                                                List<EntryRelation> relations) {
        return DictionaryResponse.builder()
                .id(e.getId())
                .text(e.getText())
                .entryType(e.getEntryType())
                .reading(e.getReading())
                .meaningVn(e.getMeaningVn())
                .jlptLevel(e.getJlptLevel())
                .explanationShort(e.getExplanationShort())
                .createdAt(e.getCreatedAt())
                .examples(examples.stream().map(ExampleDto::from).toList())
                .relations(relations.stream().map(RelationDto::from).toList())
                .build();
    }
}
