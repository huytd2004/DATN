package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hust.huy.backend.model.entity.FlashcardDeck;
import vn.hust.huy.backend.model.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link FlashcardDeck}.
 */
@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, UUID> {

    List<FlashcardDeck> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<FlashcardDeck> findByIdAndUser(UUID id, User user);
}
