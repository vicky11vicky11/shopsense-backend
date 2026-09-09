package com.shopsense.catalogservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInformationResponse {

    private UUID id;

    private UUID productId;

    private String attributeName;

    private String attributeValue;

    private Instant createdAt;

    private Instant updatedAt;
}