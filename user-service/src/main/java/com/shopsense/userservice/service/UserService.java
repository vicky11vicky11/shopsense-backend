package com.shopsense.userservice.service;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse createCustomer( UserRequest userRequest);

    UserResponse createSeller( UserRequest userRequest);

    UserResponse createAdmin( UserRequest userRequest);

    PageResponse<UserResponse> getAllUsersByRole( UserRole role, Pageable pageable );

    UserResponse getCustomer( UUID customerId );

    UserResponse getSeller( UUID sellerId );

    UserResponse getAdmin( UUID adminId );

    boolean isCustomerExists( UUID customerId );

    boolean isSellerExists( UUID sellerId );

    UserResponse updateUser( UUID userId, UserRequest userRequest );

    void deleteUser( UUID userId );

}
