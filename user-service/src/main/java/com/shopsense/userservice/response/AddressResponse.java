package com.shopsense.userservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private UUID id;

    private UUID userId;

    private String addressName;

    private String recipientName;

    private String phone;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private boolean defaultAddress;

    private Instant createdAt;

    private Instant updatedAt;
}
