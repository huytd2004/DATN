package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.FlashcardRequest;
import vn.hust.huy.backend.dto.request.ReviewRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Flashcard operations.
 * All methods resolve the current user from the JWT SecurityContext internally.
 */
public interface FlashcardService {

    /** Returns all flashcards belonging to the current user across all decks. */
    ApiResponse<List<FlashcardResponse>> getMyFlashcards();

    /** Returns flashcards inside a specific deck owned by the current user. */
    ApiResponse<List<FlashcardResponse>> getByDeck(UUID deckId);

    /** Returns a single flashcard by ID, including SRS state. */
    ApiResponse<FlashcardResponse> getById(UUID id);

    /** Creates a flashcard inside a deck owned by the current user. Also initialises SRS state. */
    ApiResponse<FlashcardResponse> create(FlashcardRequest request);

    /** Updates the content (front/back) of a flashcard owned by the current user. */
    ApiResponse<FlashcardResponse> update(UUID id, FlashcardRequest request);

    /** Submits a review result, runs SM-2, and updates SRS state + card status. */
    ApiResponse<FlashcardResponse> submitReview(UUID id, ReviewRequest request);

    /** Deletes a flashcard owned by the current user. */
    ApiResponse<Void> delete(UUID id);
}

