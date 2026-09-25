package com.shopsense.mediaservice.entity;


import com.shopsense.mediaservice.enums.MediaType;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "media")
public class Media extends BaseMongoEntity {

    @Indexed(unique = true)
    private String publicId;

    private String url;

    private String secureUrl;

    private String format;

    private Integer width;

    private Integer height;

    private Long bytes;

    private MediaType mediaType;

}
