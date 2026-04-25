package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for a single comment (flat view — replies are fetched via parentId).
 */
@Getter
@Builder
public class CommentResponse {

    private UUID id;
    private UUID entryId;
    private UUID parentId;

    /** Minimal author info — no sensitive data. */
    private UUID authorId;
    private String authorUsername;

    private String content;
    private Instant createdAt;

    public static CommentResponse fromEntity(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .entryId(c.getEntry().getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .authorId(c.getUser().getId())
                .authorUsername(c.getUser().getUsername())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
