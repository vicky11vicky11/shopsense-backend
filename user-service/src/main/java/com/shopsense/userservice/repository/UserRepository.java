package com.shopsense.userservice.repository;

import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByIdAndRole( String id, UserRole role );

    Page<User> findAllByRole( UserRole role, Pageable pageable );

}
