package vn.hust.huy.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class TutorControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    private final File base = new File("uploads/tutor-audio/test-session");

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void cleanup() throws Exception {
        if (base.exists()) {
            Files.walk(base.toPath())
                    .map(java.nio.file.Path::toFile)
                    .sorted((a,b)->-a.compareTo(b))
                    .forEach(File::delete);
        }
    }

    @Test
    void audioStreamingReturnsFile() throws Exception {
        base.mkdirs();
        File f = new File(base, "sample.txt");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write("hello".getBytes());
        }

        mvc.perform(get("/api/v1/tutor/sessions/audio/test-session/sample.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void sendMessageOversizedAudioReturns413() throws Exception {
        byte[] huge = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile audio = new MockMultipartFile("audio", "big.webm", "audio/webm", huge);
        MockMultipartFile metadata = new MockMultipartFile("metadata", "metadata", "application/json", "{}".getBytes());

        mvc.perform(multipart("/api/v1/tutor/sessions/{id}/messages", java.util.UUID.randomUUID().toString())
                        .file(metadata)
                        .file(audio)
                        .with(user("test@example.com")))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void sendMessageUnsupportedMimeReturns415() throws Exception {
        byte[] data = "hello".getBytes();
        MockMultipartFile audio = new MockMultipartFile("audio", "file.bin", "text/plain", data);
        MockMultipartFile metadata = new MockMultipartFile("metadata", "metadata", "application/json", "{}".getBytes());

        mvc.perform(multipart("/api/v1/tutor/sessions/{id}/messages", java.util.UUID.randomUUID().toString())
                        .file(metadata)
                        .file(audio)
                        .with(user("test@example.com")))
                .andExpect(status().isUnsupportedMediaType());
    }
}
