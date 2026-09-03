package com.shopsense.mediaservice.response;

import com.shopsense.mediaservice.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    private String publicId;

    private String url;

    private String secureUrl;

    private String format;

    private Integer width;

    private Integer height;

    private Long bytes;

    private MediaType mediaType;
}