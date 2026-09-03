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
@RequestMapping(AppUrl.SELLER_URL)
public class SellerController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createSeller( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createSeller(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getSeller( @PathVariable String id ) {
        UserResponse userResponse = userService.getSeller(id);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateSeller( @PathVariable String id, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller( @PathVariable String id ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent()
                .build();
    }

}
