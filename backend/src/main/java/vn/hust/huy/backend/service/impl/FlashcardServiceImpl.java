package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.FlashcardRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.FlashcardDeck;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.FlashcardDeckRepository;
import vn.hust.huy.backend.repository.FlashcardRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.FlashcardService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final UserRepository userRepository;

    // ── Get my flashcards ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FlashcardResponse>> getMyFlashcards() {
        User currentUser = resolveCurrentUser();

        List<FlashcardResponse> data = flashcardRepository
                .findAllByUserWithDeck(currentUser)
                .stream()
                .map(FlashcardResponse::fromEntity)
                .toList();

        log.debug("Fetched {} flashcard(s) for user '{}'", data.size(), currentUser.getEmail());
        return ApiResponse.success(data, "Lấy danh sách flashcard thành công");
    }

    // ── Get by deck ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FlashcardResponse>> getByDeck(UUID deckId) {
        User currentUser = resolveCurrentUser();

        // Verify deck ownership
        flashcardDeckRepository.findByIdAndUser(deckId, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        List<FlashcardResponse> data = flashcardRepository
                .findAllByDeckIdAndUser(deckId, currentUser)
                .stream()
                .map(FlashcardResponse::fromEntity)
                .toList();

        log.debug("Fetched {} flashcard(s) in deck id={}", data.size(), deckId);
        return ApiResponse.success(data, "Lấy danh sách flashcard theo bộ thẻ thành công");
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<FlashcardResponse> create(FlashcardRequest request) {
        User currentUser = resolveCurrentUser();

        FlashcardDeck deck = flashcardDeckRepository
                .findByIdAndUser(request.getDeckId(), currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        Flashcard card = Flashcard.builder()
                .deck(deck)
                .user(currentUser)
                .frontText(request.getFrontText())
                .frontReading(request.getFrontReading())
                .backText(request.getBackText())
                .backNotes(request.getBackNotes())
                .build();

        Flashcard saved = flashcardRepository.save(card);
        log.info("User '{}' added flashcard to deck '{}'", currentUser.getEmail(), deck.getName());

        return ApiResponse.success(FlashcardResponse.fromEntity(saved), "Thêm flashcard thành công", 201);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> delete(UUID id) {
        User currentUser = resolveCurrentUser();

        Flashcard card = flashcardRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        flashcardRepository.delete(card);
        log.info("User '{}' deleted flashcard id={}", currentUser.getEmail(), id);

        return ApiResponse.success(null, "Xóa flashcard thành công");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
