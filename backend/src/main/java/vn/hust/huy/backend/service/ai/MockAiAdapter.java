package vn.hust.huy.backend.service.ai;

import org.springframework.stereotype.Component;
import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.model.entity.ConversationSession;

import java.util.List;
import java.util.UUID;

@Component
public class MockAiAdapter implements AiAdapter {

    @Override
    public MessageResponse buildInitialMessage(ConversationSession session, TutorSessionRequest request) {
        return MessageResponse.builder()
                .id(UUID.randomUUID().toString())
                .role("assistant")
                .content("こんにちは。今日は" + (session.getScenarioName() == null ? "会話" : session.getScenarioName()) + "の練習をします。始めましょう！")
                .suggestions(List.of("はい、準備できました", "まだ準備できていません"))
                .build();
    }

    @Override
    public MessageResponse generateReply(ConversationSession session, MessageRequest messageRequest, List<String> recentLogs) {
        String user = messageRequest.getContent() == null ? "" : messageRequest.getContent();
        String reply = "(Mock AI) " + (user.isBlank() ? "どうしましたか？" : user + " — 良いですね。次に〜") ;
        return MessageResponse.builder()
                .id(UUID.randomUUID().toString())
                .role("assistant")
                .content(reply)
                .suggestions(List.of("Gợi ý 1", "Gợi ý 2"))
                .build();
    }
}
