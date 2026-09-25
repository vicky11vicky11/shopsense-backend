package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    @NotNull
    private UUID sellerId;

    @NotNull
    private UUID addressId;

    @NotBlank
    @Size(max = 200)
    private String storeName;

    private UUID storeImageId;
}