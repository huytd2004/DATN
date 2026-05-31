package vn.hust.huy.backend.service.media;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface AudioStorageService {
    /**
     * Save audio file for a session and return the relative filename (not full URL).
     */
    String saveAudio(String sessionId, MultipartFile file) throws Exception;

    Path load(String sessionId, String filename);
}
