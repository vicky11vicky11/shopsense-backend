package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank
    @Size(max = 200)
    private String categoryName;

    @Size(max = 1000)
    private String categoryDescription;

    private String categoryImageId;

    private UUID parentCategoryId;
}