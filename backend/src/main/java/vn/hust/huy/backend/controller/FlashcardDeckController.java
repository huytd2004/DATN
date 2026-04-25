package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.FlashcardDeckRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardDeckResponse;
import vn.hust.huy.backend.service.FlashcardDeckService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for FlashcardDeck.
 *
 * <ul>
 *   <li>GET    /api/v1/decks           – danh sách bộ thẻ của user hiện tại</li>
 *   <li>POST   /api/v1/decks           – tạo bộ thẻ mới</li>
 *   <li>PUT    /api/v1/decks/{id}      – cập nhật bộ thẻ</li>
 *   <li>DELETE /api/v1/decks/{id}      – xóa bộ thẻ (cascade flashcards)</li>
 * </ul>
 *
 * All endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
public class FlashcardDeckController {

    private final FlashcardDeckService flashcardDeckService;

    // ── GET /api/v1/decks ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardDeckResponse>>> getMyDecks() {
        return ResponseEntity.ok(flashcardDeckService.getMyDecks());
    }

    // ── POST /api/v1/decks ─────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardDeckResponse>> create(
            @Valid @RequestBody FlashcardDeckRequest request) {

        ApiResponse<FlashcardDeckResponse> response = flashcardDeckService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── PUT /api/v1/decks/{id} ─────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardDeckResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FlashcardDeckRequest request) {

        return ResponseEntity.ok(flashcardDeckService.update(id, request));
    }

    // ── DELETE /api/v1/decks/{id} ──────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(flashcardDeckService.delete(id));
    }
}
