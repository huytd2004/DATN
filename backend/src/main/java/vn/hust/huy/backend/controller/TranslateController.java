package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hust.huy.backend.dto.request.QuickTranslateRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.DeepTranslateResponse;
import vn.hust.huy.backend.dto.response.QuickTranslateResponse;
import vn.hust.huy.backend.service.TranslateService;

@RestController
@RequestMapping("/api/v1/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    @PostMapping("/quick")
    public ResponseEntity<ApiResponse<QuickTranslateResponse>> quickTranslate(
            @Valid @RequestBody QuickTranslateRequest request) {
        return ResponseEntity.ok(translateService.quickTranslate(request));
    }

    @PostMapping("/deep")
    public ResponseEntity<ApiResponse<DeepTranslateResponse>> deepTranslate(
            @Valid @RequestBody QuickTranslateRequest request) {
        return ResponseEntity.ok(translateService.deepTranslate(request));
    }
}
