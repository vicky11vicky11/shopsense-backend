package com.shopsense.catalogservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

    private UUID id;

    private String brandName;

    private String brandDescription;

    private String brandImageId;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}