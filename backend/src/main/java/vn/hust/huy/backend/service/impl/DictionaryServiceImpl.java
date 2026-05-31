package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.model.entity.EntryRelation;
import vn.hust.huy.backend.model.entity.Example;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.repository.DictionaryEntryRepository;
import vn.hust.huy.backend.repository.EntryRelationRepository;
import vn.hust.huy.backend.repository.ExampleRepository;
import vn.hust.huy.backend.service.DictionaryService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryEntryRepository dictionaryEntryRepository;
    private final ExampleRepository exampleRepository;
    private final EntryRelationRepository entryRelationRepository;

    // ── Search (paginated + type-filtered) ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DictionaryResponse>> search(String query, EntryType type, Pageable pageable) {
        Page<DictionaryEntry> page;

        boolean hasQuery = query != null && !query.isBlank();

        if (hasQuery) {
            // Full-text search — use separate methods to avoid JPQL null-binding issue
            // with PostgreSQL native enum (NAMED_ENUM) when type is null
            if (type != null) {
                page = dictionaryEntryRepository.searchByType(query.trim(), type, pageable);
            } else {
                page = dictionaryEntryRepository.searchAll(query.trim(), pageable);
            }
        } else if (type != null) {
            // No keyword, but filter by type
            page = dictionaryEntryRepository.findByEntryTypeOrderByTextAsc(type, pageable);
        } else {
            // No keyword, no filter — return all (paginated)
            page = dictionaryEntryRepository.findAllByOrderByTextAsc(pageable);
        }

        // Light mapping — no enrichment in list results to avoid N+1
        List<DictionaryResponse> data = page.getContent()
                .stream()
                .map(DictionaryResponse::fromEntity)
                .toList();

        log.debug("Dictionary search q='{}' type={} page={} → {}/{} results",
                query, type, pageable.getPageNumber(), data.size(), page.getTotalElements());

        return ApiResponse.success(data, "Tìm kiếm thành công");
    }

    // ── Get by ID (enriched) ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DictionaryResponse> getById(UUID id) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DICTIONARY_NOT_FOUND));

        List<Example> examples = exampleRepository.findByEntryId(id);
        List<EntryRelation> relations = entryRelationRepository.findBySourceIdOrderByOrderIndexAsc(id);

        DictionaryResponse response = DictionaryResponse.fromEntity(entry, examples, relations);
        log.debug("getById {} → {} examples, {} relations", id, examples.size(), relations.size());

        return ApiResponse.success(response, "Lấy thông tin từ thành công");
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<DictionaryResponse> create(DictionaryRequest request) {
        if (dictionaryEntryRepository.existsByTextIgnoreCase(request.getText())) {
            throw new AppException(ErrorCode.WORD_ALREADY_EXISTS);
        }

        DictionaryEntry entry = DictionaryEntry.builder()
                .text(request.getText())
                .entryType(request.getEntryType())
                .reading(request.getReading())
                .meaningVn(request.getMeaningVn())
                .jlptLevel(request.getJlptLevel())
                .explanationShort(request.getExplanationShort())
                .build();

        DictionaryEntry saved = dictionaryEntryRepository.save(entry);
        log.info("Created dictionary entry: '{}' ({})", saved.getText(), saved.getEntryType());

        return ApiResponse.success(DictionaryResponse.fromEntity(saved), "Thêm từ mới thành công", 201);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<DictionaryResponse> update(UUID id, DictionaryRequest request) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DICTIONARY_NOT_FOUND));

        if (!entry.getText().equalsIgnoreCase(request.getText())
                && dictionaryEntryRepository.existsByTextIgnoreCase(request.getText())) {
            throw new AppException(ErrorCode.WORD_ALREADY_EXISTS);
        }

        entry.setText(request.getText());
        entry.setEntryType(request.getEntryType());
        entry.setReading(request.getReading());
        entry.setMeaningVn(request.getMeaningVn());
        entry.setJlptLevel(request.getJlptLevel());
        entry.setExplanationShort(request.getExplanationShort());

        DictionaryEntry updated = dictionaryEntryRepository.save(entry);
        log.info("Updated dictionary entry id={}", id);

        return ApiResponse.success(DictionaryResponse.fromEntity(updated), "Cập nhật từ thành công");
    }
}
