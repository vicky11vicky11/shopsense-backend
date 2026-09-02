package com.shopsense.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {

    private String id;

    private String userId;

    private String addressName;

    private String recipientName;

    private String phone;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private boolean isDefault;

    private Instant createdAt;

    private Instant updatedAt;
}
