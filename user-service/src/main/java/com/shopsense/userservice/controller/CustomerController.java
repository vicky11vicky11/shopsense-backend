package com.shopsense.userservice.controller;

import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
import com.shopsense.userservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> getCustomer( @PathVariable String customerId ) {
        UserResponse userResponse = userService.getCustomer(customerId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/exists/{customerId}")
    public ResponseEntity<Boolean> getCustomerExists( @PathVariable String customerId ) {
        boolean customerExists = userService.isCustomerExists(customerId);
        return ResponseEntity.ok(customerExists);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<UserResponse> updateCustomer( @PathVariable String customerId, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(customerId, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer( @PathVariable String customerId ) {
        userService.deleteUser(customerId);
        return ResponseEntity.noContent()
                .build();
    }

}
