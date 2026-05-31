package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.FlashcardRequest;
import vn.hust.huy.backend.dto.request.ReviewRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.FlashcardResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.FlashcardDeck;
import vn.hust.huy.backend.model.entity.SrsDetail;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.model.enums.FlashcardStatus;
import vn.hust.huy.backend.repository.FlashcardDeckRepository;
import vn.hust.huy.backend.repository.FlashcardRepository;
import vn.hust.huy.backend.repository.SrsDetailRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.FlashcardService;
import vn.hust.huy.backend.util.SM2Algorithm;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository    flashcardRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final SrsDetailRepository    srsDetailRepository;
    private final UserRepository         userRepository;

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
                .findAllByDeckIdAndDeckUser(deckId, currentUser)
                .stream()
                .map(FlashcardResponse::fromEntity)
                .toList();

        log.debug("Fetched {} flashcard(s) in deck id={}", data.size(), deckId);
        return ApiResponse.success(data, "Lấy danh sách flashcard theo bộ thẻ thành công");
    }

    // ── Get by ID (with SRS info) ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FlashcardResponse> getById(UUID id) {
        User currentUser = resolveCurrentUser();

        Flashcard card = flashcardRepository.findByIdAndDeckUser(id, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        SrsDetail srs = getOrCreateSrs(card);

        log.debug("Fetched flashcard id={} for user '{}'", id, currentUser.getEmail());
        return ApiResponse.success(FlashcardResponse.fromEntityWithSrs(card, srs),
                "Lấy chi tiết flashcard thành công");
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
                .frontText(request.getFrontText())
                .frontReading(request.getFrontReading())
                .backText(request.getBackText())
                .backNotes(request.getBackNotes())
                .build();

        Flashcard saved = flashcardRepository.save(card);

        // Initialise SRS state automatically on creation
        SrsDetail srs = SrsDetail.builder()
                .flashcard(saved)
                .easeFactor(SM2Algorithm.DEFAULT_EF)
                .intervalDays(0)
                .repetitions(0)
                .build();
        srsDetailRepository.save(srs);

        log.info("User '{}' created flashcard in deck '{}'", currentUser.getEmail(), deck.getName());
        return ApiResponse.success(FlashcardResponse.fromEntityWithSrs(saved, srs),
                "Thêm flashcard thành công", 201);
    }

    // ── Update content ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<FlashcardResponse> update(UUID id, FlashcardRequest request) {
        User currentUser = resolveCurrentUser();

        Flashcard card = flashcardRepository.findByIdAndDeckUser(id, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        card.setFrontText(request.getFrontText());
        card.setFrontReading(request.getFrontReading());
        card.setBackText(request.getBackText());
        card.setBackNotes(request.getBackNotes());

        Flashcard updated = flashcardRepository.save(card);
        SrsDetail srs = getOrCreateSrs(updated);

        log.info("User '{}' updated flashcard id={}", currentUser.getEmail(), id);
        return ApiResponse.success(FlashcardResponse.fromEntityWithSrs(updated, srs),
                "Cập nhật flashcard thành công");
    }

    // ── Submit review (SM-2) ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<FlashcardResponse> submitReview(UUID id, ReviewRequest request) {
        User currentUser = resolveCurrentUser();

        Flashcard card = flashcardRepository.findByIdAndDeckUser(id, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        SrsDetail srs = getOrCreateSrs(card);

        int quality = request.getRating().toQuality();

        // Run SM-2 algorithm
        SM2Algorithm.ReviewResult result = SM2Algorithm.calculate(
                srs.getEaseFactor(),
                srs.getRepetitions(),
                srs.getIntervalDays(),
                quality
        );

        // Persist updated SRS state
        srs.setEaseFactor(result.easeFactor());
        srs.setRepetitions(result.repetitions());
        srs.setIntervalDays(result.intervalDays());
        srs.setNextReview(result.nextReview());
        srsDetailRepository.save(srs);

        // Update card status based on SM-2 outcome:
        // - repetitions == 0 → failed review or just starting → LEARNING
        // - repetitions == 1 → first success, short interval → still LEARNING
        // - repetitions >= 2 → graduated to spaced-repetition REVIEW queue
        FlashcardStatus newStatus = (result.repetitions() >= 2)
                ? FlashcardStatus.REVIEW
                : FlashcardStatus.LEARNING;

        card.setStatus(newStatus);
        Flashcard saved = flashcardRepository.save(card);

        log.info("User '{}' reviewed flashcard id={} → rating={}, newInterval={}d, nextReview={}",
                currentUser.getEmail(), id, request.getRating(),
                result.intervalDays(), result.nextReview());

        return ApiResponse.success(FlashcardResponse.fromEntityWithSrs(saved, srs),
                "Ôn tập thành công. Lần ôn tiếp theo: " + result.intervalDays() + " ngày nữa.");
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> delete(UUID id) {
        User currentUser = resolveCurrentUser();

        Flashcard card = flashcardRepository
                .findByIdAndDeckUser(id, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_NOT_FOUND));

        flashcardRepository.delete(card);
        log.info("User '{}' deleted flashcard id={}", currentUser.getEmail(), id);

        return ApiResponse.success(null, "Xóa flashcard thành công");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Loads the SrsDetail for a flashcard, creating a default one if it does not exist yet.
     * This handles flashcards that were created before SRS tracking was introduced.
     */
    private SrsDetail getOrCreateSrs(Flashcard card) {
        return srsDetailRepository.findByFlashcard(card)
                .orElseGet(() -> {
                    log.warn("SrsDetail missing for flashcard id={}. Creating default.", card.getId());
                    SrsDetail defaultSrs = SrsDetail.builder()
                            .flashcard(card)
                            .easeFactor(SM2Algorithm.DEFAULT_EF)
                            .intervalDays(0)
                            .repetitions(0)
                            .build();
                    return srsDetailRepository.save(defaultSrs);
                });
    }

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
