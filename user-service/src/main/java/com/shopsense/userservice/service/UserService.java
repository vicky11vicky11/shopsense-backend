package com.shopsense.userservice.service;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createCustomer( UserRequest userRequest);

    UserResponse createSeller( UserRequest userRequest);

    UserResponse createAdmin( UserRequest userRequest);

    PageResponse<UserResponse> getAllUsersByRole( UserRole role, Pageable pageable );

    UserResponse getCustomer( String id );

    UserResponse getSeller( String id );

    UserResponse getAdmin( String id );

    UserResponse updateUser( String id, UserRequest userRequest );

    void deleteUser( String id );
}
