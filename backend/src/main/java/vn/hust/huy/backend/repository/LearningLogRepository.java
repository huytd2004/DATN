package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.LearningLog;

import java.util.List;
import java.util.UUID;

public interface LearningLogRepository extends JpaRepository<LearningLog, UUID> {
    List<LearningLog> findBySession_IdOrderByCreatedAtAsc(UUID sessionId);
    void deleteBySession_Id(UUID sessionId);
}
