package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    boolean existsBySellerIdAndAddressId( UUID sellerId, UUID addressId );

    Page<Store> findBySellerId( UUID sellerId, Pageable pageable );

    Optional<Store> findByIdAndActive( UUID id, Boolean active );
}