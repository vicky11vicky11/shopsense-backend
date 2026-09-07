package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {

    @NotBlank
    private String sellerId;

    @NotBlank
    private String addressId;

    @NotBlank
    @Size(max = 200)
    private String storeName;

    private String storeImageId;
}