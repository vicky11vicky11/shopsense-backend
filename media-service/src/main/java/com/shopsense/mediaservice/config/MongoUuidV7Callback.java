package com.shopsense.mediaservice.config;

import com.github.f4b6a3.uuid.UuidCreator;
import com.shopsense.mediaservice.entity.BaseMongoEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class MongoUuidV7Callback implements BeforeConvertCallback<BaseMongoEntity> {

    @Override
    public BaseMongoEntity onBeforeConvert( BaseMongoEntity entity, String collection ) {
        if ( entity.getId() == null ) {
            entity.setId(UuidCreator.getTimeOrderedEpoch());
        }
        return entity;
    }
}