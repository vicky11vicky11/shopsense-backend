package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreRequest {

    @NotBlank
    @Size(max = 200)
    private String storeName;

    private String storeImageId;

}