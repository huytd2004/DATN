package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.CommentRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.CommentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Comment operations.
 */
public interface CommentService {

    /** Get all top-level comments for a dictionary entry. */
    ApiResponse<List<CommentResponse>> getCommentsByEntry(UUID entryId);

    /** Get all replies for a parent comment. */
    ApiResponse<List<CommentResponse>> getReplies(UUID parentId);

    /** Create a new comment or reply. User resolved from SecurityContext. */
    ApiResponse<CommentResponse> create(CommentRequest request);

    /** Delete a comment. Only the author can delete their own comment. */
    ApiResponse<Void> delete(UUID commentId);
}
