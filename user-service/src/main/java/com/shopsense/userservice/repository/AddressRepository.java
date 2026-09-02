package com.shopsense.userservice.repository;

import com.shopsense.userservice.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {

    Page<Address> findAllByUserId( String userId, Pageable pageable );

    Optional<Address> findByUserIdAndIsDefaultTrue( String userId);

    Optional<Address> findByIdAndUserId( String id, String userId );
}
