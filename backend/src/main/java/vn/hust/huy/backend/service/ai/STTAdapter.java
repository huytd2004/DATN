package vn.hust.huy.backend.service.ai;

import org.springframework.web.multipart.MultipartFile;

public interface STTAdapter {
    String transcribe(MultipartFile audio) throws Exception;
}
