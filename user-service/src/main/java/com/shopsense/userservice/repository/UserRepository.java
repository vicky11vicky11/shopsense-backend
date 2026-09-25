package com.shopsense.userservice.repository;

import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.enums.UserRole;
import com.shopsense.userservice.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {

    Optional<User> findByIdAndRole( UUID id, UserRole role );

    Page<User> findAllByRole( UserRole role, Pageable pageable );

    boolean existsByIdAndRoleAndStatus( UUID id, UserRole role, UserStatus status );
}
