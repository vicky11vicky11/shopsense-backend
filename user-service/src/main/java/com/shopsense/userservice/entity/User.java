package com.shopsense.userservice.entity;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@CompoundIndex(name = "uk_user_email_non_deleted", def = "{'email': 1}", unique = true, partialFilter = "{'status': {'$in': ['ACTIVE', 'INACTIVE', 'SUSPENDED', 'BLOCKED']}}")
@CompoundIndex(name = "uk_user_phone_non_deleted", def = "{'phone': 1}", unique = true, partialFilter = "{'status': {'$in': ['ACTIVE', 'INACTIVE', 'SUSPENDED', 'BLOCKED']}}")
public class User extends BaseMongoEntity {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    @Indexed
    private UserRole role;

    private UserStatus status;

    private boolean emailVerified;

    private boolean phoneVerified;

    private UUID profileImageId;

    @Version
    private Long version;

}
