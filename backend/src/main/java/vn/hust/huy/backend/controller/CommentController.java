package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.CommentRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.CommentResponse;
import vn.hust.huy.backend.service.CommentService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Comment module.
 *
 * <ul>
 *   <li>GET  /api/v1/comments?entryId={id}         – lấy top-level comments của 1 từ</li>
 *   <li>GET  /api/v1/comments/{id}/replies          – lấy replies của 1 comment</li>
 *   <li>POST /api/v1/comments                       – đăng comment mới (hoặc reply)</li>
 *   <li>DELETE /api/v1/comments/{id}               – xóa comment (chỉ tác giả/ADMIN)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ── GET /api/v1/comments?entryId= ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByEntry(
            @RequestParam UUID entryId) {

        return ResponseEntity.ok(commentService.getCommentsByEntry(entryId));
    }

    // ── GET /api/v1/comments/{id}/replies ─────────────────────────────────────

    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @PathVariable UUID id) {

        return ResponseEntity.ok(commentService.getReplies(id));
    }

    // ── POST /api/v1/comments ─────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @Valid @RequestBody CommentRequest request) {

        ApiResponse<CommentResponse> response = commentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/v1/comments/{id} ──────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.delete(id));
    }
}
