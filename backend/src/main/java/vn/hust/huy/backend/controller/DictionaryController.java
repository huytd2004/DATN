package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.service.DictionaryService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Dictionary module.
 *
 * <ul>
 *   <li>GET  /api/v1/dictionary              – paginated search (q, type, page, size)</li>
 *   <li>GET  /api/v1/dictionary/{id}         – single enriched entry</li>
 *   <li>POST /api/v1/dictionary              – create (ADMIN only)</li>
 *   <li>PUT  /api/v1/dictionary/{id}         – update (ADMIN only)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    // ── GET /api/v1/dictionary?q=&type=word&page=0&size=20 ──────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<DictionaryResponse>>> search(
            @RequestParam(value = "q",    required = false)               String query,
            @RequestParam(value = "type", required = false)               EntryType type,
            @RequestParam(value = "page", required = false, defaultValue = "0")  int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100)); // cap at 100
        return ResponseEntity.ok(dictionaryService.search(query, type, pageable));
    }

    // ── GET /api/v1/dictionary/{id} ──────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dictionaryService.getById(id));
    }

    // ── POST /api/v1/dictionary ──────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponse>> create(
            @Valid @RequestBody DictionaryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dictionaryService.create(request));
    }

    // ── PUT /api/v1/dictionary/{id} ──────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DictionaryRequest request) {

        return ResponseEntity.ok(dictionaryService.update(id, request));
    }
}
