package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.User;

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
            JOIN FETCH f.deck
            WHERE f.user = :user
            ORDER BY f.createdAt DESC
            """)
    List<Flashcard> findAllByUserWithDeck(@Param("user") User user);

    /**
     * Fetches all flashcards inside a specific deck for the current user.
     */
    @Query("""
            SELECT f FROM Flashcard f
            JOIN FETCH f.deck d
            WHERE d.id = :deckId AND f.user = :user
            ORDER BY f.createdAt DESC
            """)
    List<Flashcard> findAllByDeckIdAndUser(@Param("deckId") UUID deckId, @Param("user") User user);

    /**
     * Ownership-scoped lookup — prevents a user from accessing another user's flashcard.
     */
    Optional<Flashcard> findByIdAndUser(UUID id, User user);
}
