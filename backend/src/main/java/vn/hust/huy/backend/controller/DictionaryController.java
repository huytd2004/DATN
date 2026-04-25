package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.DictionaryRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DictionaryResponse;
import vn.hust.huy.backend.service.DictionaryService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Dictionary module.
 *
 * <ul>
 *   <li>GET  /api/v1/dictionary          – list all / search</li>
 *   <li>POST /api/v1/dictionary          – create a new entry</li>
 *   <li>PUT  /api/v1/dictionary/{id}     – update an entry</li>
 * </ul>
 *
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    // ── GET /api/v1/dictionary?q= ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<DictionaryResponse>>> search(
            @RequestParam(value = "q", required = false) String query) {

        return ResponseEntity.ok(dictionaryService.search(query));
    }

    // ── POST /api/v1/dictionary ────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponse>> create(
            @Valid @RequestBody DictionaryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dictionaryService.create(request));
    }

    // ── PUT /api/v1/dictionary/{id} ────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DictionaryRequest request) {

        return ResponseEntity.ok(dictionaryService.update(id, request));
    }
}
