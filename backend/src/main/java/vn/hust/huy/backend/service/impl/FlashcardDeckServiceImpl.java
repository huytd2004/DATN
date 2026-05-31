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
import vn.hust.huy.backend.repository.FlashcardRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.FlashcardDeckService;

import org.springframework.data.domain.PageRequest;
import vn.hust.huy.backend.dto.response.FlashcardResponse;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardDeckServiceImpl implements FlashcardDeckService {

    private final FlashcardDeckRepository flashcardDeckRepository;
    private final FlashcardRepository     flashcardRepository;
    private final UserRepository          userRepository;

    // ── Get my decks (with card stats) ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FlashcardDeckResponse>> getMyDecks() {
        User currentUser = resolveCurrentUser();

        List<FlashcardDeck> decks = flashcardDeckRepository
                .findAllByUserOrderByCreatedAtDesc(currentUser);

        // Single aggregate query → map of deckId → { status → count }
        Map<UUID, Map<String, Long>> statsMap = buildStatsMap(currentUser);
        Map<UUID, Long> masteredMap = buildMasteredMap(currentUser);
        Map<UUID, Long> dueMap = buildDueMap(currentUser, Instant.now());

        List<FlashcardDeckResponse> data = decks.stream()
                .map(deck -> {
                    Map<String, Long> statusCounts = statsMap.getOrDefault(deck.getId(), Map.of());
                    int learningCount = statusCounts.getOrDefault("learning", 0L).intValue();
                    int reviewCount = statusCounts.getOrDefault("review", 0L).intValue();
                    int fallbackDue = learningCount + reviewCount;
                    int due = dueMap.getOrDefault(deck.getId(), (long) fallbackDue).intValue();

                    return FlashcardDeckResponse.fromEntityWithStatsAndDue(
                            deck, statusCounts, masteredMap.getOrDefault(deck.getId(), 0L), due);
                })
                .toList();

        log.debug("Fetched {} deck(s) with stats for user '{}'", data.size(), currentUser.getEmail());
        return ApiResponse.success(data, "Lấy danh sách bộ thẻ thành công");
    }

    // ── Get single deck by ID (with card stats) ───────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FlashcardDeckResponse> getById(UUID deckId) {
        User currentUser = resolveCurrentUser();

        FlashcardDeck deck = flashcardDeckRepository.findByIdAndUser(deckId, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        // Fetch stats for this specific deck and compute dueFromSrs
        Map<UUID, Map<String, Long>> statsMap = buildStatsMap(currentUser);
        Map<UUID, Long> masteredMap = buildMasteredMap(currentUser);
        Map<UUID, Long> dueMap = buildDueMap(currentUser, Instant.now());

        Map<String, Long> statusCounts = statsMap.getOrDefault(deck.getId(), Map.of());
        int learningCount = statusCounts.getOrDefault("learning", 0L).intValue();
        int reviewCount = statusCounts.getOrDefault("review", 0L).intValue();
        int fallbackDue = learningCount + reviewCount;
        int due = dueMap.getOrDefault(deck.getId(), (long) fallbackDue).intValue();

        FlashcardDeckResponse data = FlashcardDeckResponse.fromEntityWithStatsAndDue(
                deck, statusCounts, masteredMap.getOrDefault(deck.getId(), 0L), due);

        log.debug("Fetched deck id={} with stats for user '{}'", deckId, currentUser.getEmail());
        return ApiResponse.success(data, "Lấy chi tiết bộ thẻ thành công");
    }

        /**
         * Helper that executes the aggregate query to compute how many cards are due
         * (s.next_review <= now and status in (learning, review)) grouped by deck.
         */
        private Map<UUID, Long> buildDueMap(User user, Instant now) {
                List<Object[]> rows = flashcardRepository.countDueByDeck(user.getId(), now);
                Map<UUID, Long> dueMap = new HashMap<>();
                for (Object[] row : rows) {
                        UUID deckId = (UUID) row[0];
                        long count = ((Number) row[1]).longValue();
                        dueMap.put(deckId, count);
                }
                return dueMap;
        }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<FlashcardResponse>> getDueCards(UUID deckId, int maxNew) {
        User currentUser = resolveCurrentUser();

        flashcardDeckRepository.findByIdAndUser(deckId, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        int safeMaxNew = Math.max(0, maxNew);
        Instant now = Instant.now();

        List<FlashcardResponse> dueCards = flashcardRepository
                .findDueByDeckAndUser(
                        deckId,
                        currentUser,
                        now,
                        FlashcardStatus.LEARNING,
                        FlashcardStatus.REVIEW
                )
                .stream()
                .map(card -> FlashcardResponse.fromEntityWithSrs(card, card.getSrsDetail()))
                .toList();

        List<FlashcardResponse> newCards = safeMaxNew == 0
                ? List.of()
                : flashcardRepository
                        .findNewByDeckAndUser(
                                deckId,
                                currentUser,
                                FlashcardStatus.NEW_CARD,
                                PageRequest.of(0, safeMaxNew)
                        )
                        .stream()
                        .map(card -> FlashcardResponse.fromEntityWithSrs(card, card.getSrsDetail()))
                        .toList();

        List<FlashcardResponse> data = new java.util.ArrayList<>(dueCards.size() + newCards.size());
        data.addAll(dueCards);
        data.addAll(newCards);

        log.debug("Fetched {} due card(s) for deck id={} and user '{}'", data.size(), deckId, currentUser.getEmail());
        return ApiResponse.success(data, "Lấy danh sách thẻ cần học hôm nay thành công");
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

        // Newly created deck has no cards yet → empty stats
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

    /**
     * Executes a single aggregate query and returns a nested map:
     * {@code deckId → { "new" → count, "learning" → count, "review" → count }}.
     */
    private Map<UUID, Map<String, Long>> buildStatsMap(User user) {
        List<Object[]> rows = flashcardRepository
                .countByStatusGroupedByDeck(user.getId());

        Map<UUID, Map<String, Long>> statsMap = new HashMap<>();
        for (Object[] row : rows) {
            UUID   deckId = (UUID)   row[0];
            String status = (String) row[1];
            long   count  = ((Number) row[2]).longValue();

            statsMap.computeIfAbsent(deckId, k -> new HashMap<>())
                    .put(status, count);
        }
        return statsMap;
    }

    /**
     * Executes aggregate query for mastered cards (status='review') grouped by deck.
     * Returns map: {@code deckId → masteredCount}.
     */
    private Map<UUID, Long> buildMasteredMap(User user) {
        List<Object[]> rows = flashcardRepository
                .countMasteredByDeck(user.getId());

        Map<UUID, Long> masteredMap = new HashMap<>();
        for (Object[] row : rows) {
            UUID deckId = (UUID) row[0];
            long count  = ((Number) row[1]).longValue();
            masteredMap.put(deckId, count);
        }
        return masteredMap;
    }

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
