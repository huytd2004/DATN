package vn.hust.huy.backend.service.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MockSTTAdapter implements STTAdapter {
    @Override
    public String transcribe(MultipartFile audio) throws Exception {
        // Mock: return empty or placeholder
        return "";
    }
}
