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
@RequestMapping(AppUrl.SELLER_URL)
public class SellerController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createSeller( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createSeller(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<UserResponse> getSeller( @PathVariable UUID sellerId ) {
        UserResponse userResponse = userService.getSeller(sellerId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/exists/{sellerId}")
    public ResponseEntity<Boolean> getCustomerExists( @PathVariable UUID sellerId ) {
        boolean customerExists = userService.isSellerExists(sellerId);
        return ResponseEntity.ok(customerExists);
    }

    @PutMapping("/{sellerId}")
    public ResponseEntity<UserResponse> updateSeller( @PathVariable UUID sellerId, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(sellerId, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{sellerId}")
    public ResponseEntity<Void> deleteSeller( @PathVariable UUID sellerId ) {
        userService.deleteUser(sellerId);
        return ResponseEntity.noContent()
                .build();
    }

}
