package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.EntryRelation;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link EntryRelation} — kanji components, compounds, synonyms, radicals, etc.
 */
public interface EntryRelationRepository extends JpaRepository<EntryRelation, UUID> {

    /**
     * Returns all relations where the given entry is the SOURCE (i.e. word → its components).
     * Sorted by order_index ASC nulls last so components appear in the correct stroke order.
     */
    List<EntryRelation> findBySourceIdOrderByOrderIndexAsc(UUID sourceId);
}
