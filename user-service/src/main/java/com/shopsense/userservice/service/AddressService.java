package com.shopsense.userservice.service;

import com.shopsense.userservice.request.AddressRequest;
import com.shopsense.userservice.response.AddressResponse;
import com.shopsense.userservice.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AddressService {
    AddressResponse createAddress( @Valid AddressRequest addressRequest );

    PageResponse<AddressResponse> getAllAddresses( UUID userId, Pageable pageable );

    AddressResponse getAddressById( UUID addressId, UUID userId );

    boolean isAddressExists( UUID addressId, UUID userId );

    AddressResponse updateAddress( UUID addressId, @Valid AddressRequest addressRequest );

    void deleteAddress( UUID addressId, UUID userId );

}
