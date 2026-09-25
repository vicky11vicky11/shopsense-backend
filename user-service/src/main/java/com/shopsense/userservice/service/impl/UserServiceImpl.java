package com.shopsense.userservice.service.impl;

import com.shopsense.userservice.client.MediaServiceClient;
import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.enums.MediaType;
import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import com.shopsense.userservice.exception.ResourceNotFoundException;
import com.shopsense.userservice.mapper.UserMapper;
import com.shopsense.userservice.repository.UserRepository;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final MediaServiceClient mediaServiceClient;

    @Override
    @Transactional
    public UserResponse createCustomer( UserRequest userRequest ) {
        return createUser(userRequest, UserRole.CUSTOMER);
    }

    @Override
    @Transactional
    public UserResponse createSeller( UserRequest userRequest ) {
        return createUser(userRequest, UserRole.SELLER);
    }

    @Override
    @Transactional
    public UserResponse createAdmin( UserRequest userRequest ) {
        return createUser(userRequest, UserRole.ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsersByRole( UserRole role, Pageable pageable ) {
        Page<User> users = role == null ? userRepository.findAll(pageable) : userRepository.findAllByRole(role, pageable);
        log.info("Fetched users. Role {}, Count {}, Sort {}, Page {}, Size {}", role == null ? "ALL" : role, users.getTotalElements(), pageable.getSort(), pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(users, userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCustomer( UUID userId ) {
        return getUserById(userId, UserRole.CUSTOMER);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getSeller( UUID userId ) {
        return getUserById(userId, UserRole.SELLER);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAdmin( UUID userId ) {
        return getUserById(userId, UserRole.ADMIN);
    }

    @Override
    public boolean isCustomerExists( UUID customerId ) {
        boolean userExists = isUserExists(customerId, UserRole.CUSTOMER);
        log.info("Customer {} exists {}", customerId, userExists);
        return userExists;
    }

    @Override
    public boolean isSellerExists( UUID sellerId ) {
        boolean userExists = isUserExists(sellerId, UserRole.SELLER);
        log.info("Seller {} exists {}", sellerId, userExists);
        return userExists;
    }

    @Override
    @Transactional
    public UserResponse updateUser( UUID userId, UserRequest userRequest ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if ( !user.getEmail()
                .equalsIgnoreCase(userRequest.getEmail()) ) {
            user.setEmail(userRequest.getEmail());
            user.setEmailVerified(false);
        }
        if ( !user.getPhone()
                .equalsIgnoreCase(userRequest.getPhone()) ) {
            user.setPhone(userRequest.getPhone());
            user.setPhoneVerified(false);
        }
        if ( !user.getFirstName()
                .equals(userRequest.getFirstName()) ) {
            user.setFirstName(userRequest.getFirstName());
        }
        if ( !user.getLastName()
                .equals(userRequest.getLastName()) ) {
            user.setLastName(userRequest.getLastName());
        }
        if ( !user.getProfileImageId()
                .equals(userRequest.getProfileImageId()) ) {
            boolean imagedExist = mediaServiceClient.imageExist(userRequest.getProfileImageId(), MediaType.USER);
            if ( !imagedExist ) {
                throw new ResourceNotFoundException("Image not found");
            }
            user.setProfileImageId(userRequest.getProfileImageId());
        }
        User savedUser = userRepository.save(user);
        log.info("Updated user with id {}", userId);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser( UUID userId ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if ( user.getStatus() == UserStatus.DELETED ) {
            throw new ResourceNotFoundException("User not found");
        }
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("Deleted user with id {}", userId);
    }

    private UserResponse createUser( UserRequest userRequest, UserRole userRole ) {
        User user = userMapper.toEntity(userRequest);
        if ( user.getProfileImageId() != null ) {
            boolean imagedExist = mediaServiceClient.imageExist(userRequest.getProfileImageId(), MediaType.USER);
            if ( !imagedExist ) {
                throw new ResourceNotFoundException("Image not found");
            }
        }
        user.setRole(userRole);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileImageId(userRequest.getProfileImageId());
        User savedUser = userRepository.save(user);
        log.info("Created user with id {} and role {}", savedUser.getId(), savedUser.getRole());
        return userMapper.toResponse(savedUser);
    }

    private UserResponse getUserById( UUID userId, UserRole userRole ) {
        User user = userRepository.findByIdAndRole(userId, userRole)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if ( user.getStatus() == UserStatus.DELETED ) {
            throw new ResourceNotFoundException("User not found");
        }
        log.info("Fetched user with id {}", userId);
        return userMapper.toResponse(user);
    }

    private boolean isUserExists( UUID userId, UserRole userRole ) {
        boolean exists = userRepository.existsByIdAndRoleAndStatus(userId, userRole, UserStatus.ACTIVE);
        log.info("User {} with role {} exists {}", userId, userRole, exists);
        return exists;
    }
}
