package com.shopsense.userservice.controller;

import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
import com.shopsense.userservice.util.AppUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrl.CUSTOMER_URL)
public class CustomerController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createCustomer( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createCustomer(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<UserResponse> getCustomer( @PathVariable UUID customerId ) {
        UserResponse userResponse = userService.getCustomer(customerId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/exists/{customerId}")
    public ResponseEntity<Boolean> getCustomerExists( @PathVariable UUID customerId ) {
        boolean customerExists = userService.isCustomerExists(customerId);
        return ResponseEntity.ok(customerExists);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<UserResponse> updateCustomer( @PathVariable UUID customerId, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(customerId, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer( @PathVariable UUID customerId ) {
        userService.deleteUser(customerId);
        return ResponseEntity.noContent()
                .build();
    }

}
