package com.shopsense.userservice.controller;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.PageResponse;
import com.shopsense.userservice.response.UserResponse;
import com.shopsense.userservice.service.UserService;
import com.shopsense.userservice.util.AppUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrl.ADMIN_URL)
public class AdminController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create( @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.createAdmin(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<UserResponse> getById( @PathVariable UUID adminId ) {
        UserResponse userResponse = userService.getAdmin(adminId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<UserResponse>> getUsers( @RequestParam(required = false) UserRole role, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable ) {
        PageResponse<UserResponse> userResponses = userService.getAllUsersByRole(role, pageable);
        return ResponseEntity.ok(userResponses);
    }

    @PutMapping("/{adminId}")
    public ResponseEntity<UserResponse> update( @PathVariable UUID adminId, @Valid @RequestBody UserRequest userRequest ) {
        UserResponse userResponse = userService.updateUser(adminId, userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> delete( @PathVariable UUID adminId ) {
        userService.deleteUser(adminId);
        return ResponseEntity.noContent()
                .build();
    }

}
