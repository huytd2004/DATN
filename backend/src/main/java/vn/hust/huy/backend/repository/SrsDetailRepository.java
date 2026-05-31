package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.SrsDetail;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link SrsDetail}.
 * Primary key = flashcard_id (shared PK with flashcards table).
 */
public interface SrsDetailRepository extends JpaRepository<SrsDetail, UUID> {

    Optional<SrsDetail> findByFlashcard(Flashcard flashcard);
}
