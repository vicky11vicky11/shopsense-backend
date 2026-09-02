package com.shopsense.userservice.service.impl;

import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import com.shopsense.userservice.exceptions.ResourceNotFoundException;
import com.shopsense.userservice.mapper.UserMapper;
import com.shopsense.userservice.repository.UserRepository;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

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
        if ( role == null ) {
            log.info("Fetched all users. Sort {}, Page {}, Size {}", pageable.getSort(), pageable.getPageNumber(), pageable.getPageSize());
            return PageResponse.from(userRepository.findAll(pageable), userMapper::toResponse);
        }
        log.info("Fetched all users by role {}. Sort {}, Page {}, Size {}", role, pageable.getSort(), pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(userRepository.findAllByRole(role, pageable), userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCustomer( String id ) {
        return getUserById(id, UserRole.CUSTOMER);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getSeller( String id ) {
        return getUserById(id, UserRole.SELLER);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAdmin( String id ) {
        return getUserById(id, UserRole.ADMIN);
    }

    @Override
    @Transactional
    public UserResponse updateUser( String id, UserRequest userRequest ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by id : " + id));
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
        User savedUser = userRepository.save(user);
        log.info("Updated user with id {}", id);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser( String id ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by id : " + id));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("Deleted user with id {}", id);
    }

    private UserResponse createUser( UserRequest userRequest, UserRole userRole ) {
        User user = userMapper.toEntity(userRequest);
        user.setRole(userRole);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("Created user with id {} and role {}", savedUser.getId(), savedUser.getRole());
        return userMapper.toResponse(savedUser);
    }


    private UserResponse getUserById( String id, UserRole userRole ) {
        User user = userRepository.findByIdAndRole(id, userRole)
                .orElseThrow(() -> new NoSuchElementException("Resource not found with id : " + id));
        if ( user.getStatus() == UserStatus.DELETED ) {
            throw new ResourceNotFoundException("User not found by id : " + id);
        }
        log.info("Fetched user with id {}", id);
        return userMapper.toResponse(user);
    }
}
