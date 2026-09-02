package com.shopsense.userservice.service.impl;

import com.shopsense.userservice.entity.Address;
import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.enums.UserStatus;
import com.shopsense.userservice.exceptions.ResourceNotFoundException;
import com.shopsense.userservice.exceptions.UserAccountBlockedException;
import com.shopsense.userservice.exceptions.UserAccountSuspendedException;
import com.shopsense.userservice.mapper.AddressMapper;
import com.shopsense.userservice.repository.AddressRepository;
import com.shopsense.userservice.repository.UserRepository;
import com.shopsense.userservice.request.AddressRequest;
import com.shopsense.userservice.response.AddressResponse;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    private final UserRepository userRepository;

    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse createAddress( AddressRequest addressRequest ) {
        User user = userRepository.findById(addressRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateUser(user);
        Address address = addressMapper.toEntity(addressRequest);
        if ( address.isDefault() ) {
            addressRepository.findByUserIdAndIsDefaultTrue(addressRequest.getUserId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }
        Address savedAddress = addressRepository.save(address);
        log.info("Address created: {}", savedAddress.getId());
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AddressResponse> getAllAddresses( String userId, Pageable pageable ) {
        Page<Address> addressList = addressRepository.findAllByUserId(userId, pageable);
        log.info("Fetched All Addresses for user {} with Size {}, Page {} and Sort {}.", userId, pageable.getPageSize(), pageable.getPageNumber(), pageable.getSort());
        return PageResponse.from(addressList, addressMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById( String addressId, String userId ) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        log.info("Fetched address by id {}", address.getId());
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress( String addressId, AddressRequest addressRequest ) {
        Address address = addressRepository.findByIdAndUserId(addressId, addressRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        User user = userRepository.findById(addressRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + addressRequest.getUserId()));
        validateUser(user);
        if ( !address.isDefault() && addressRequest.isDefault() ) {
            addressRepository.findByUserIdAndIsDefaultTrue(addressRequest.getUserId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }
        addressMapper.updateEntity(addressRequest, address);
        Address savedAddress = addressRepository.save(address);
        log.info("Address updated: {}", savedAddress.getId());
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress( String addressId, String userId ) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + userId));
        validateUser(user);
        addressRepository.delete(address);
        log.info("Address deleted with id {}", address.getId());
    }

    private void validateUser( User user ) {
        if ( user.getStatus() == UserStatus.DELETED ) {
            throw new ResourceNotFoundException("User not found with Id: " + user.getId());
        }
        if ( user.getStatus() == UserStatus.BLOCKED ) {
            throw new UserAccountBlockedException("Your account is blocked. You cannot perform this action until your account is unblocked.");
        }
        if ( user.getStatus() == UserStatus.SUSPENDED ) {
            throw new UserAccountSuspendedException("Your account is suspended. You cannot perform this action until your account is reactivated.");
        }
    }
}
