package com.shopsense.mediaservice.request;

import com.shopsense.mediaservice.enums.MediaType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkMediaUploadRequest {

    @NotEmpty(message = "At least one image is required")
    @Size(max = 10, message = "Maximum 10 images are allowed")
    private List<MultipartFile> images;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;
}