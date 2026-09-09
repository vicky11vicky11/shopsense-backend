package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.ProductInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductInformationRepository extends JpaRepository<ProductInformation, UUID> {

    Page<ProductInformation> findByProductIdOrderByAttributeNameAsc( UUID productId, Pageable pageable );

    Optional<ProductInformation> findByProductIdAndAttributeName( UUID productId, String attributeName );

    boolean existsByProductIdAndAttributeName( UUID productId, String attributeName );

}