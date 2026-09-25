package com.shopsense.mediaservice.repository;

import com.shopsense.mediaservice.entity.Media;
import com.shopsense.mediaservice.enums.MediaType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends MongoRepository<Media, UUID> {

    List<Media> findAllByIdIn( List<UUID> ids );

    boolean existsByIdAndMediaType( UUID id, MediaType mediaType );
}
