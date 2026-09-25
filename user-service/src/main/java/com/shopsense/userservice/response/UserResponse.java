package com.shopsense.userservice.response;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private UserRole role;

    private UserStatus status;

    private boolean emailVerified;

    private boolean phoneVerified;

    private UUID profileImageId;

    private Instant createdAt;
    
    private Instant updatedAt;

}
