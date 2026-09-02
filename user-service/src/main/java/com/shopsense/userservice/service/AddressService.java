package com.shopsense.userservice.service;

import com.shopsense.userservice.request.AddressRequest;
import com.shopsense.userservice.response.AddressResponse;
import com.shopsense.userservice.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

public interface AddressService {
    AddressResponse createAddress( @Valid AddressRequest addressRequest );

    PageResponse<AddressResponse> getAllAddresses( String userId, Pageable pageable );

    AddressResponse getAddressById( String addressId, String userId );

    AddressResponse updateAddress( String addressId, @Valid AddressRequest addressRequest );

    void deleteAddress( String addressId, String userId );
}
