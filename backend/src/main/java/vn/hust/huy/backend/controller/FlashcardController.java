package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.FlashcardRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;
import vn.hust.huy.backend.service.FlashcardService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Flashcard module.
 *
 * <ul>
 *   <li>GET    /api/v1/flashcards        – get the current user's flashcards</li>
 *   <li>POST   /api/v1/flashcards        – add a flashcard (link a dictionary entry)</li>
 *   <li>DELETE /api/v1/flashcards/{id}   – remove a flashcard</li>
 * </ul>
 *
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 * All responses follow the ApiResponse envelope defined in instructions.md.
 */
@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    // ── GET /api/v1/flashcards              – all flashcards of current user ────
    // ── GET /api/v1/flashcards?deckId={id} – flashcards inside a specific deck ─

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcards(
            @RequestParam(value = "deckId", required = false) UUID deckId) {

        ApiResponse<List<FlashcardResponse>> response = (deckId != null)
                ? flashcardService.getByDeck(deckId)
                : flashcardService.getMyFlashcards();

        return ResponseEntity.ok(response);
    }

    // ── POST /api/v1/flashcards ────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> create(
            @Valid @RequestBody FlashcardRequest request) {

        ApiResponse<FlashcardResponse> response = flashcardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/v1/flashcards/{id} ────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ApiResponse<Void> response = flashcardService.delete(id);
        return ResponseEntity.ok(response);
    }
}
