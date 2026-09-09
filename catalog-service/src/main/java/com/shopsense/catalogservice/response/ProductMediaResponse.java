package com.shopsense.catalogservice.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMediaResponse {

    private UUID id;

    private UUID productId;

    private String mediaId;

    private Integer displayOrder;

    private boolean primaryImage;
}