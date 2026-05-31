package vn.hust.huy.backend.service.ai;

public interface TTSAdapter {
    /**
     * Synthesize speech for given text and return filename under session folder.
     */
    String synthesize(String sessionId, String text) throws Exception;
}
