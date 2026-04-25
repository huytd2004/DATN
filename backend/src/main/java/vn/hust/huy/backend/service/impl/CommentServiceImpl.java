package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.CommentRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.CommentResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.Comment;
import vn.hust.huy.backend.model.entity.DictionaryEntry;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.CommentRepository;
import vn.hust.huy.backend.repository.DictionaryEntryRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.CommentService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DictionaryEntryRepository dictionaryEntryRepository;
    private final UserRepository userRepository;

    // ── Get top-level comments ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CommentResponse>> getCommentsByEntry(UUID entryId) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new AppException(ErrorCode.DICTIONARY_NOT_FOUND));

        List<CommentResponse> data = commentRepository.findTopLevelByEntry(entry)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();

        log.debug("Fetched {} top-level comment(s) for entry id={}", data.size(), entryId);
        return ApiResponse.success(data, "Lấy bình luận thành công");
    }

    // ── Get replies ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CommentResponse>> getReplies(UUID parentId) {
        // Validate parent exists
        commentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        List<CommentResponse> data = commentRepository.findRepliesByParentId(parentId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();

        log.debug("Fetched {} repl(ies) for comment id={}", data.size(), parentId);
        return ApiResponse.success(data, "Lấy trả lời thành công");
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<CommentResponse> create(CommentRequest request) {
        User currentUser = resolveCurrentUser();

        DictionaryEntry entry = dictionaryEntryRepository.findById(request.getEntryId())
                .orElseThrow(() -> new AppException(ErrorCode.DICTIONARY_NOT_FOUND));

        // Resolve optional parent comment
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .entry(entry)
                .user(currentUser)
                .parent(parent)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("User '{}' posted comment on entry id={}", currentUser.getUsername(), entry.getId());

        return ApiResponse.success(CommentResponse.fromEntity(saved), "Đăng bình luận thành công", 201);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> delete(UUID commentId) {
        User currentUser = resolveCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // Only the author (or ADMIN) can delete
        if (!comment.getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != vn.hust.huy.backend.model.enums.Role.ADMIN) {
            throw new AppException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        commentRepository.delete(comment);
        log.info("Comment id={} deleted by user '{}'", commentId, currentUser.getUsername());

        return ApiResponse.success(null, "Xóa bình luận thành công");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
