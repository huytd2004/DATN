package vn.hust.huy.backend.service.ai;

import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.model.entity.ConversationSession;

import java.util.List;

public interface AiAdapter {
    MessageResponse buildInitialMessage(ConversationSession session, TutorSessionRequest request);
    MessageResponse generateReply(ConversationSession session, MessageRequest messageRequest, List<String> recentLogs);
}
