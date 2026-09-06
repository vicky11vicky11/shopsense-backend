package com.shopsense.mediaservice.repository;

import com.shopsense.mediaservice.entity.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {

    List<Media> findAllByIdIn( List<String> ids );

}
