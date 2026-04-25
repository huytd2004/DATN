package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Dictionary operations.
 */
public interface DictionaryService {

    /** Search entries by text / reading / meaning. If query is blank, returns all. */
    ApiResponse<List<DictionaryResponse>> search(String query);

    /** Create a new entry. Throws WORD_ALREADY_EXISTS (409) on duplicate text. */
    ApiResponse<DictionaryResponse> create(DictionaryRequest request);

    /** Update an entry. Throws DICTIONARY_NOT_FOUND (404) if id not found. */
    ApiResponse<DictionaryResponse> update(UUID id, DictionaryRequest request);
}
