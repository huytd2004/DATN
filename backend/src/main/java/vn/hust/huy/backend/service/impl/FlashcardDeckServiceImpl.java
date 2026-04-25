package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.FlashcardDeckRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardDeckResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.FlashcardDeck;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.FlashcardDeckRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.FlashcardDeckService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardDeckServiceImpl implements FlashcardDeckService {

    private final FlashcardDeckRepository flashcardDeckRepository;
    private final UserRepository userRepository;

    // ── Get my decks ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FlashcardDeckResponse>> getMyDecks() {
        User currentUser = resolveCurrentUser();

        List<FlashcardDeckResponse> data = flashcardDeckRepository
                .findAllByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(FlashcardDeckResponse::fromEntity)
                .toList();

        log.debug("Fetched {} deck(s) for user '{}'", data.size(), currentUser.getEmail());
        return ApiResponse.success(data, "Lấy danh sách bộ thẻ thành công");
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<FlashcardDeckResponse> create(FlashcardDeckRequest request) {
        User currentUser = resolveCurrentUser();

        FlashcardDeck deck = FlashcardDeck.builder()
                .user(currentUser)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .build();

        FlashcardDeck saved = flashcardDeckRepository.save(deck);
        log.info("User '{}' created deck '{}'", currentUser.getEmail(), saved.getName());

        return ApiResponse.success(FlashcardDeckResponse.fromEntity(saved), "Tạo bộ thẻ thành công", 201);
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<FlashcardDeckResponse> update(UUID deckId, FlashcardDeckRequest request) {
        User currentUser = resolveCurrentUser();

        FlashcardDeck deck = flashcardDeckRepository.findByIdAndUser(deckId, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setPublic(request.isPublic());

        FlashcardDeck updated = flashcardDeckRepository.save(deck);
        log.info("User '{}' updated deck id={}", currentUser.getEmail(), deckId);

        return ApiResponse.success(FlashcardDeckResponse.fromEntity(updated), "Cập nhật bộ thẻ thành công");
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> delete(UUID deckId) {
        User currentUser = resolveCurrentUser();

        FlashcardDeck deck = flashcardDeckRepository.findByIdAndUser(deckId, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        flashcardDeckRepository.delete(deck);
        log.info("User '{}' deleted deck id={}", currentUser.getEmail(), deckId);

        return ApiResponse.success(null, "Xóa bộ thẻ thành công");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
