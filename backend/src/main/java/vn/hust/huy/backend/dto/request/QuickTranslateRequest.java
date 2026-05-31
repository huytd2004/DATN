package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class QuickTranslateRequest {

    @NotBlank(message = "Văn bản cần dịch không được để trống")
    @Size(max = 5000, message = "Văn bản không được vượt quá 5000 ký tự")
    private String text;

    @Size(max = 10, message = "Mã ngôn ngữ nguồn không hợp lệ")
    private String sourceLang;

    @Size(max = 10, message = "Mã ngôn ngữ đích không hợp lệ")
    private String targetLang;

    @Size(max = 120, message = "Sentence ID không hợp lệ")
    private String sentenceId;
}
