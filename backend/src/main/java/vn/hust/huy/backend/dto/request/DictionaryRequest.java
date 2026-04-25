package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import vn.hust.huy.backend.model.enums.EntryType;
import vn.hust.huy.backend.model.enums.JlptLevel;

/**
 * Request body for creating or updating a dictionary entry.
 */
@Getter
public class DictionaryRequest {

    @NotBlank(message = "Từ/Văn bản không được để trống")
    @Size(max = 500, message = "Từ không được vượt quá 500 ký tự")
    private String text;

    @NotNull(message = "Loại từ không được để trống")
    private EntryType entryType;

    @Size(max = 500, message = "Phiên âm không được vượt quá 500 ký tự")
    private String reading;

    @NotBlank(message = "Nghĩa tiếng Việt không được để trống")
    private String meaningVn;

    private JlptLevel jlptLevel;

    private String explanationShort;
}
