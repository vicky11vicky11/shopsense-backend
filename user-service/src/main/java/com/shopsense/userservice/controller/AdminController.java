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
@RequestMapping(AppUrl.ADMIN_URL)
public class AdminController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createAdmin( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createAdmin(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getAdmin( @PathVariable String id ) {
        UserResponse userResponse = userService.getAdmin(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers( @RequestParam(required = false) UserRole role, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable ) {
        PageResponse<UserResponse> userResponses = userService.getAllUsersByRole(role, pageable);
        return ResponseEntity.ok(userResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateAdmin( @PathVariable String id, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser( @PathVariable String id ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent()
                .build();
    }

}
