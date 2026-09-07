package com.shopsense.catalogservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;

    private String categoryName;

    private String categoryDescription;

    private String categoryImageId;

    private UUID parentCategoryId;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}