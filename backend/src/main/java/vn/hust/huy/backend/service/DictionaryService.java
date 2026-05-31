package vn.hust.huy.backend.service;

import org.springframework.data.domain.Pageable;
import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;
import vn.hust.huy.backend.model.enums.EntryType;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Dictionary operations.
 */
public interface DictionaryService {

    /**
     * Paginated search across text/reading/meaningVn.
     * @param query     search keyword (nullable / blank = return all)
     * @param type      filter by entry type (nullable = all types)
     * @param pageable  page number, size, sort
     */
    ApiResponse<List<DictionaryResponse>> search(String query, EntryType type, Pageable pageable);

    /**
     * Returns a single enriched entry (with examples + relations).
     * Throws DICTIONARY_NOT_FOUND (404) if not found.
     */
    ApiResponse<DictionaryResponse> getById(UUID id);

    /** Create a new entry. Throws WORD_ALREADY_EXISTS (409) on duplicate text. */
    ApiResponse<DictionaryResponse> create(DictionaryRequest request);

    /** Update an entry. Throws DICTIONARY_NOT_FOUND (404) if id not found. */
    ApiResponse<DictionaryResponse> update(UUID id, DictionaryRequest request);
}
