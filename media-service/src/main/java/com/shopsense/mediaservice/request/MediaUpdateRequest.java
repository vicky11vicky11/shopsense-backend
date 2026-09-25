package com.shopsense.mediaservice.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class MediaUpdateRequest {

    @NotNull(message = "Image is required")
    private MultipartFile image;

}