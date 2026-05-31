package vn.hust.huy.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link Flashcard}.
 */
        public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    /**
     * Fetches all flashcards for a given user with the deck eagerly loaded
     * to prevent N+1 queries.
     */
    @Query("""
            SELECT f FROM Flashcard f
            JOIN FETCH f.deck d
            WHERE d.user = :user
            ORDER BY f.createdAt DESC
            """)
    List<Flashcard> findAllByUserWithDeck(@Param("user") User user);

    /**
     * Fetches all flashcards inside a specific deck for the current user.
     */
    @Query("""
            SELECT f FROM Flashcard f
            JOIN FETCH f.deck d
            WHERE d.id = :deckId AND d.user = :user
            ORDER BY f.createdAt DESC
            """)
    List<Flashcard> findAllByDeckIdAndDeckUser(@Param("deckId") UUID deckId, @Param("user") User user);

    /**
     * Ownership-scoped lookup — prevents a user from accessing another user's flashcard.
     */
        Optional<Flashcard> findByIdAndDeckUser(UUID id, User user);

    /**
     * Aggregates card counts grouped by deck and status for a given user.
     * Returns rows of [deck_id (UUID), status (String), count (Long)].
     *
     * <p>Uses a native query because the {@code status} column is stored as
     * {@code varchar} and the AttributeConverter is not applied in JPQL projections.
     * A single query replaces O(N×3) derived-method calls for N decks.
     */
    @Query(value = """
            SELECT f.deck_id, f.status, COUNT(*) AS cnt
            FROM flashcards f
            JOIN flashcard_decks d ON d.id = f.deck_id
            WHERE d.user_id = :userId
            GROUP BY deck_id, status
            """, nativeQuery = true)
    List<Object[]> countByStatusGroupedByDeck(@Param("userId") UUID userId);

    /**
     * Counts flashcards with status='review' (mastered cards in SRS cycle)
     * grouped by deck for a given user.
     * Returns rows of [deck_id (UUID), count (Long)].
     */
    @Query(value = """
            SELECT f.deck_id, COUNT(*) AS cnt
            FROM flashcards f
            JOIN flashcard_decks d ON d.id = f.deck_id
            WHERE d.user_id = :userId AND f.status = 'review'
            GROUP BY deck_id
            """, nativeQuery = true)
    List<Object[]> countMasteredByDeck(@Param("userId") UUID userId);

    /**
     * Counts flashcards that are due (s.next_review <= now) grouped by deck for a given user.
     * Only considers cards with status in ('learning','review').
     * Returns rows of [deck_id (UUID), count (Long)].
     */
    @Query(value = """
            SELECT f.deck_id, COUNT(*) AS cnt
            FROM flashcards f
            JOIN srs_details s ON s.flashcard_id = f.id
            JOIN flashcard_decks d ON d.id = f.deck_id
            WHERE d.user_id = :userId AND s.next_review <= :now AND f.status IN ('learning','review')
            GROUP BY f.deck_id
            """, nativeQuery = true)
    List<Object[]> countDueByDeck(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Returns cards that need review today for a deck (learning always included, review only when due).
     */
    @Query("""
            SELECT f
            FROM Flashcard f
            JOIN FETCH f.deck d
            JOIN FETCH f.srsDetail s
            WHERE d.id = :deckId
              AND d.user = :user
              AND s.nextReview <= :now
              AND (f.status = :learningStatus OR f.status = :reviewStatus)
            ORDER BY CASE WHEN f.status = :reviewStatus THEN 0 ELSE 1 END, s.nextReview ASC, f.createdAt ASC
            """)
    List<Flashcard> findDueByDeckAndUser(@Param("deckId") UUID deckId,
                                         @Param("user") User user,
                                         @Param("now") java.time.Instant now,
                                         @Param("learningStatus") vn.hust.huy.backend.model.enums.FlashcardStatus learningStatus,
                                         @Param("reviewStatus") vn.hust.huy.backend.model.enums.FlashcardStatus reviewStatus);

    /**
     * Returns the first N new cards for a deck.
     */
    @Query("""
            SELECT f
            FROM Flashcard f
            JOIN FETCH f.deck d
            JOIN FETCH f.srsDetail s
            WHERE d.id = :deckId
              AND d.user = :user
              AND f.status = :newStatus
            ORDER BY f.createdAt ASC
            """)
    List<Flashcard> findNewByDeckAndUser(@Param("deckId") UUID deckId,
                                         @Param("user") User user,
                                         @Param("newStatus") vn.hust.huy.backend.model.enums.FlashcardStatus newStatus,
                                         Pageable pageable);
}
