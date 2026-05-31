package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.TutorSessionResult;

import java.util.Optional;
import java.util.UUID;

public interface TutorSessionResultRepository extends JpaRepository<TutorSessionResult, UUID> {
    Optional<TutorSessionResult> findBySession_Id(UUID sessionId);
}