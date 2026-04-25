package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hust.huy.backend.model.entity.Comment;
import vn.hust.huy.backend.model.entity.DictionaryEntry;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Fetches all top-level comments for an entry (parent IS NULL),
     * eagerly loading author to prevent N+1.
     */
    @Query("""
            SELECT c FROM Comment c
            JOIN FETCH c.user
            WHERE c.entry = :entry AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findTopLevelByEntry(@Param("entry") DictionaryEntry entry);

    /**
     * Fetches all replies for a given parent comment,
     * eagerly loading author.
     */
    @Query("""
            SELECT c FROM Comment c
            JOIN FETCH c.user
            WHERE c.parent.id = :parentId
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findRepliesByParentId(@Param("parentId") UUID parentId);
}
