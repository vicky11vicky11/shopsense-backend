package com.shopsense.userservice.controller;

import com.shopsense.userservice.request.AddressRequest;
import com.shopsense.userservice.response.AddressResponse;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.service.AddressService;
import com.shopsense.userservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppUrl.ADDRESS_URL)
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress( @Valid @RequestBody AddressRequest addressRequest ) {
        AddressResponse addressResponse = addressService.createAddress(addressRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressResponse);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<PageResponse<AddressResponse>> getAllAddresses( @PathVariable String userId, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable ) {
        PageResponse<AddressResponse> addressResponses = addressService.getAllAddresses(userId, pageable);
        return ResponseEntity.ok(addressResponses);
    }

    @GetMapping("/{addressId}/user/{userId}")
    public ResponseEntity<AddressResponse> getAddressById( @PathVariable String addressId, @PathVariable String userId ) {
        AddressResponse addressResponse = addressService.getAddressById(addressId, userId);
        return ResponseEntity.ok(addressResponse);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress( @PathVariable String addressId, @Valid @RequestBody AddressRequest addressRequest ) {
        AddressResponse addressResponse = addressService.updateAddress(addressId, addressRequest);
        return ResponseEntity.ok(addressResponse);
    }

    @DeleteMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Void> deleteAddress( @PathVariable String addressId, @PathVariable String userId ) {
        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.noContent()
                .build();
    }

}
