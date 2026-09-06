package com.shopsense.mediaservice.entity;


import com.shopsense.mediaservice.enums.MediaType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "media")
public class Media {

    @Id
    private String id;

    @Indexed(unique = true)
    private String publicId;

    private String url;

    private String secureUrl;

    private String format;

    private Integer width;

    private Integer height;

    private Long bytes;

    private MediaType mediaType;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
