package com.shopsense.orderservice.response;

import com.shopsense.orderservice.enums.MediaType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDetailsResponse {

    private String id;

    private String url;

    private String secureUrl;

    private String format;

    private Integer width;

    private Integer height;

    private Long bytes;

    private MediaType mediaType;

}
