package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.FlashcardDeckRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardDeckResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for FlashcardDeck operations.
 * All methods resolve current user from SecurityContext.
 */
public interface FlashcardDeckService {

    /** Get all decks owned by the current user. */
    ApiResponse<List<FlashcardDeckResponse>> getMyDecks();

    /** Get a single deck by ID with card statistics (mastered + status counts). */
    ApiResponse<FlashcardDeckResponse> getById(UUID deckId);

    /** Returns cards that should be studied today in a deck. */
    ApiResponse<List<FlashcardResponse>> getDueCards(UUID deckId, int maxNew);

    /** Create a new deck. */
    ApiResponse<FlashcardDeckResponse> create(FlashcardDeckRequest request);

    /** Update a deck owned by the current user. */
    ApiResponse<FlashcardDeckResponse> update(UUID deckId, FlashcardDeckRequest request);

    /** Delete a deck (and cascade its flashcards). */
    ApiResponse<Void> delete(UUID deckId);
}
