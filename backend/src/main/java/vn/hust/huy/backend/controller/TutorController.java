package vn.hust.huy.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import vn.hust.huy.backend.dto.request.MessageRequest;
import vn.hust.huy.backend.dto.request.TutorSessionRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.MessageResponse;
import vn.hust.huy.backend.dto.response.TutorSessionResponse;
import vn.hust.huy.backend.service.TutorService;

import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/tutor/sessions")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<TutorSessionResponse>> createSession(
            @RequestBody TutorSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TutorSessionResponse resp = tutorService.createSession(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(resp, "Session created", 201));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorSessionResponse>> getSession(@PathVariable UUID id,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {
        TutorSessionResponse resp = tutorService.getSession(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(resp, "OK"));
    }

    @PostMapping(value = "/{id}/messages", consumes = {"application/json", "multipart/form-data"})
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable UUID id,
            @RequestPart(required = false) String metadata,
            @RequestPart(required = false) org.springframework.web.multipart.MultipartFile audio,
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageRequest messageRequest = null;
        try {
            if (metadata != null && !metadata.isBlank()) {
                messageRequest = objectMapper.readValue(metadata, MessageRequest.class);
            } else if (httpRequest.getContentType() != null && httpRequest.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
                JsonNode root = objectMapper.readTree(httpRequest.getInputStream());
                JsonNode metadataNode = root.get("metadata");
                if (metadataNode != null && !metadataNode.isNull()) {
                    if (metadataNode.isTextual()) {
                        messageRequest = objectMapper.readValue(metadataNode.asText(), MessageRequest.class);
                    } else if (metadataNode.isObject()) {
                        messageRequest = objectMapper.treeToValue(metadataNode, MessageRequest.class);
                    }
                } else {
                    messageRequest = objectMapper.treeToValue(root, MessageRequest.class);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Invalid metadata", 400));
        }
        if (messageRequest == null) messageRequest = new MessageRequest();
        // Validate audio if present
        if (audio != null && !audio.isEmpty()) {
            long max = 10L * 1024L * 1024L; // 10MB
            if (audio.getSize() > max) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.error("Audio file too large", 413));
            }
            String ct = audio.getContentType();
            if (ct == null || !(ct.startsWith("audio/") || ct.equals("application/octet-stream"))) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResponse.error("Unsupported audio MIME type", 415));
            }
        }
        MessageResponse resp = tutorService.sendMessage(id, messageRequest, audio, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(resp, "OK"));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<ApiResponse<Void>> finishSession(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        // owner check will be in service
        tutorService.finishSession(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Session finished"));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<ApiResponse<vn.hust.huy.backend.dto.response.TutorResultResponse>> getResult(@PathVariable UUID id,
                                                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        var resp = tutorService.getResult(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(resp, "OK"));
    }

    @GetMapping("/audio/{sessionId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getAudio(@PathVariable String sessionId,
                                                                         @PathVariable String filename) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/tutor-audio").resolve(sessionId).resolve(filename);
            org.springframework.core.io.PathResource resource = new org.springframework.core.io.PathResource(path);
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
