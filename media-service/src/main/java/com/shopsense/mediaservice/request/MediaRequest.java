package com.shopsense.mediaservice.request;

import com.shopsense.commonservice.enums.MediaType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class MediaRequest {

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;
}