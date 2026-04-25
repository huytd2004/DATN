package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hust.huy.backend.model.entity.DictionaryEntry;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link DictionaryEntry}.
 */
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, UUID> {

    /** Duplicate check on the {@code text} field (case-insensitive). */
    boolean existsByTextIgnoreCase(String text);

    /**
     * Searches across {@code text}, {@code reading}, and {@code meaning_vn} columns.
     */
    @Query("""
            SELECT d FROM DictionaryEntry d
            WHERE LOWER(d.text)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(d.reading)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(d.meaningVn) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY d.text ASC
            """)
    List<DictionaryEntry> search(@Param("q") String query);
}
