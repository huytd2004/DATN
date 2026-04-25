package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

/**
 * Request body for creating a comment on a dictionary entry.
 * parentId is optional — null means top-level comment, non-null means reply.
 */
@Getter
public class CommentRequest {

    @NotNull(message = "ID từ điển không được để trống")
    private UUID entryId;

    /** Null = top-level comment; non-null = reply to another comment. */
    private UUID parentId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}
