package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.dto.response.TutorSessionResponse;

import java.util.UUID;

public interface TutorService {
    TutorSessionResponse createSession(TutorSessionRequest request, String userEmail);
    TutorSessionResponse getSession(UUID sessionId, String userEmail);
    MessageResponse sendMessage(UUID sessionId, MessageRequest request, org.springframework.web.multipart.MultipartFile audio, String userEmail);
    vn.hust.huy.backend.dto.response.TutorResultResponse getResult(UUID sessionId, String userEmail);
    void finishSession(UUID sessionId, String userEmail);
}
