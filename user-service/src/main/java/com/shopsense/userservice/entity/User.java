package com.shopsense.userservice.entity;

import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User extends BaseMongoEntity{

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
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
