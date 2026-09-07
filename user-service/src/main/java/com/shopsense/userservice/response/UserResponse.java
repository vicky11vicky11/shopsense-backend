package com.shopsense.userservice.response;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private UserRole role;

    private UserStatus status;

    private boolean emailVerified;

    private boolean phoneVerified;

    private String profileImageId;

    private Instant createdAt;
    
    private Instant updatedAt;

}
