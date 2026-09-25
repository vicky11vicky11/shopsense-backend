package com.shopsense.userservice.repository;

import com.shopsense.userservice.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends MongoRepository<Address, UUID> {

    Page<Address> findAllByUserId( UUID userId, Pageable pageable );

    Optional<Address> findByUserIdAndDefaultAddressTrue( UUID userId);

    Optional<Address> findByIdAndUserId( UUID id, UUID userId );

    boolean existsByIdAndUserId( UUID id, UUID userId );
}
