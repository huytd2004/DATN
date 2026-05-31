package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hust.huy.backend.model.entity.Example;

import java.util.List;
import java.util.UUID;

/**
 * Repository for example sentences linked to a {@link vn.hust.huy.backend.model.entity.DictionaryEntry}.
 */
public interface ExampleRepository extends JpaRepository<Example, UUID> {

    /** Returns all examples for a given dictionary entry. */
    List<Example> findByEntryId(UUID entryId);
}
