package com.shopsense.orderservice.response;

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

    private UUID mediaId;

    private Integer displayOrder;

    private boolean primaryImage;
}