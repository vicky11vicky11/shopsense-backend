package com.shopsense.mediaservice.request;

import com.shopsense.mediaservice.enums.MediaType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaRequest {

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;
}