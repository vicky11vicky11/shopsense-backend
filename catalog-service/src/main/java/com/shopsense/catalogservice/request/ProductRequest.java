package com.shopsense.catalogservice.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull
    private UUID storeId;

    @NotNull
    private UUID categoryId;

    private UUID brandId;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "50.00")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal price;
}