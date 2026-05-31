package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.ConversationSession;

import java.util.Optional;
import java.util.UUID;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, UUID> {
    Optional<ConversationSession> findByIdAndUser_Id(UUID id, UUID userId);
}
