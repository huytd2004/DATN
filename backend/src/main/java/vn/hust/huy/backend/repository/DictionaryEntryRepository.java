package vn.hust.huy.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.model.enums.EntryType;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link DictionaryEntry}.
 */
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, UUID> {

    /** Duplicate check on the {@code text} field (case-insensitive). */
    boolean existsByTextIgnoreCase(String text);

    /**
     * Full-text search across text/reading/meaningVn — no type filter.
     * Avoids the JPQL null-binding issue with PostgreSQL NAMED_ENUM.
     */
    @Query("""
            SELECT d FROM DictionaryEntry d
            WHERE LOWER(d.text)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(d.reading)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(d.meaningVn) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY d.text ASC
            """)
    Page<DictionaryEntry> searchAll(@Param("q") String query, Pageable pageable);

    /**
     * Full-text search across text/reading/meaningVn — filtered by a specific entryType.
     */
    @Query("""
            SELECT d FROM DictionaryEntry d
            WHERE d.entryType = :type
              AND (
                   LOWER(d.text)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(d.reading)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(d.meaningVn) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY d.text ASC
            """)
    Page<DictionaryEntry> searchByType(@Param("q") String query,
                                       @Param("type") EntryType type,
                                       Pageable pageable);

    /**
     * Returns all entries of a given type with pagination (when query is blank).
     */
    Page<DictionaryEntry> findByEntryTypeOrderByTextAsc(EntryType entryType, Pageable pageable);

    /**
     * Returns all entries with pagination (when query is blank and no type filter).
     */
    Page<DictionaryEntry> findAllByOrderByTextAsc(Pageable pageable);
}
