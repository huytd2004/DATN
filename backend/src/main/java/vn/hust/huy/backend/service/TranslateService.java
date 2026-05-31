package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.QuickTranslateRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.QuickTranslateResponse;

public interface TranslateService {
    ApiResponse<QuickTranslateResponse> quickTranslate(QuickTranslateRequest request);

    ApiResponse<vn.hust.huy.backend.dto.response.DeepTranslateResponse> deepTranslate(QuickTranslateRequest request);
}
