package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequest {

    @NotBlank
    @Size(max = 200)
    private String brandName;

    @Size(max = 1000)
    private String brandDescription;

    private String brandImageId;
}