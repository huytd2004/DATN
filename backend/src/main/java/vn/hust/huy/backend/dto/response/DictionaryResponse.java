package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.model.enums.JlptLevel;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model returned to the client for a single dictionary entry.
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
}
