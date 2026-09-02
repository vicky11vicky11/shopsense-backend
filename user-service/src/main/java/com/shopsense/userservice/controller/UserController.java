package com.shopsense.userservice.controller;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
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
@RequiredArgsConstructor
@RequestMapping(AppUrl.USER_URL)
public class UserController {

    private final UserService userService;

    @PostMapping("/customer")
    public ResponseEntity<UserResponse> createCustomer( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createCustomer(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PostMapping("/seller")
    public ResponseEntity<UserResponse> createSeller( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createSeller(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createAdmin(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsersByRole( @RequestParam(required = false) UserRole role, @PageableDefault(page = 0, size = 10, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable ) {
        PageResponse<UserResponse> userResponses = userService.getAllUsersByRole(role, pageable);
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<UserResponse> getCustomer( @PathVariable String id ) {
        UserResponse userResponse = userService.getCustomer(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/seller/{id}")
    public ResponseEntity<UserResponse> getSeller( @PathVariable String id ) {
        UserResponse userResponse = userService.getSeller(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<UserResponse> getAdmin( @PathVariable String id ) {
        UserResponse userResponse = userService.getAdmin(id);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser( @PathVariable String id,@Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(id,userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent()
                .build();
    }

}
