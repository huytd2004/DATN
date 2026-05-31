package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.FlashcardRequest;
import vn.hust.huy.backend.dto.request.ReviewRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;
import vn.hust.huy.backend.service.FlashcardService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Flashcard module.
 *
 * <ul>
 *   <li>GET    /api/v1/flashcards             – all flashcards of the current user</li>
 *   <li>GET    /api/v1/flashcards?deckId={id} – flashcards inside a specific deck</li>
 *   <li>GET    /api/v1/flashcards/{id}        – single flashcard detail + SRS info</li>
 *   <li>POST   /api/v1/flashcards             – create a flashcard (auto-creates SRS state)</li>
 *   <li>PUT    /api/v1/flashcards/{id}        – update flashcard content</li>
 *   <li>PATCH  /api/v1/flashcards/{id}/review – submit review result (SM-2)</li>
 *   <li>DELETE /api/v1/flashcards/{id}        – remove a flashcard</li>
 * </ul>
 *
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 * All responses follow the ApiResponse envelope.
 */
@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    // ── GET /api/v1/flashcards              – all cards of current user ─────────
    // ── GET /api/v1/flashcards?deckId={id} – cards inside a specific deck ──────

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcards(
            @RequestParam(value = "deckId", required = false) UUID deckId) {

        ApiResponse<List<FlashcardResponse>> response = (deckId != null)
                ? flashcardService.getByDeck(deckId)
                : flashcardService.getMyFlashcards();

        return ResponseEntity.ok(response);
    }

    // ── GET /api/v1/flashcards/{id} ───────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(flashcardService.getById(id));
    }

    // ── POST /api/v1/flashcards ───────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> create(
            @Valid @RequestBody FlashcardRequest request) {

        ApiResponse<FlashcardResponse> response = flashcardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── PUT /api/v1/flashcards/{id} ───────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FlashcardRequest request) {

        return ResponseEntity.ok(flashcardService.update(id, request));
    }

    // ── PATCH /api/v1/flashcards/{id}/review ─────────────────────────────────

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<FlashcardResponse>> submitReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest request) {

        return ResponseEntity.ok(flashcardService.submitReview(id, request));
    }

    // ── DELETE /api/v1/flashcards/{id} ───────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(flashcardService.delete(id));
    }
}
