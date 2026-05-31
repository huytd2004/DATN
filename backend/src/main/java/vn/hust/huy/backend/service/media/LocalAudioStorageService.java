package vn.hust.huy.backend.service.media;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalAudioStorageService implements AudioStorageService {

    private final Path root = Paths.get("uploads/tutor-audio");

    public LocalAudioStorageService() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public String saveAudio(String sessionId, MultipartFile file) throws Exception {
        String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path sessionDir = root.resolve(sessionId);
        Files.createDirectories(sessionDir);
        Path dest = sessionDir.resolve(filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audio", e);
        }
        return filename;
    }

    @Override
    public Path load(String sessionId, String filename) {
        return root.resolve(sessionId).resolve(filename);
    }
}
