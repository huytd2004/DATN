package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.repository.DictionaryEntryRepository;
import vn.hust.huy.backend.service.DictionaryService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryEntryRepository dictionaryEntryRepository;

    // ── Search ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DictionaryResponse>> search(String query) {
        List<DictionaryEntry> results = (query == null || query.isBlank())
                ? dictionaryEntryRepository.findAll()
                : dictionaryEntryRepository.search(query.trim());

        List<DictionaryResponse> data = results.stream()
                .map(DictionaryResponse::fromEntity)
                .toList();

        log.debug("Dictionary search '{}' → {} result(s)", query, data.size());
        return ApiResponse.success(data, "Tìm kiếm thành công");
    }

    // ── Create ─────────────────────────────────────────────────────────────────

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

    // ── Update ─────────────────────────────────────────────────────────────────

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
