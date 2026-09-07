package com.shopsense.catalogservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private UUID id;

    private String sellerId;

    private String addressId;

    private String storeName;

    private String storeImageId;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

}