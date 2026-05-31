package vn.hust.huy.backend.service.ai;

import org.springframework.stereotype.Component;
import vn.hust.huy.backend.service.media.AudioStorageService;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MockTTSAdapter implements TTSAdapter {

    private final AudioStorageService storageService;

    public MockTTSAdapter(AudioStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public String synthesize(String sessionId, String text) throws Exception {
        // Mock: create a small silent WAV file or reuse existing placeholder
        String filename = "tts-" + System.currentTimeMillis() + ".txt";
        // We'll write a text file placeholder to storage (since creating real audio is out of scope)
        Path p = storageService.load(sessionId, "__placeholder");
        // ensure directory exists
        if (!Files.exists(p.getParent())) Files.createDirectories(p.getParent());
        Path dest = p.getParent().resolve(filename);
        Files.writeString(dest, "TTS placeholder for: " + text);
        return filename;
    }
}
