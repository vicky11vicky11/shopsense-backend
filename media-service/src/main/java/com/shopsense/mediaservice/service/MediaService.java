package com.shopsense.mediaservice.service;

import com.shopsense.mediaservice.enums.MediaType;
import com.shopsense.mediaservice.request.BulkMediaUploadRequest;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaDetailsResponse;
import com.shopsense.mediaservice.response.MediaResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MediaService {

    MediaResponse uploadImage( MediaRequest mediaRequest );

    List<MediaResponse> uploadImages( BulkMediaUploadRequest mediaRequest );

    Map<MediaType,List<MediaDetailsResponse>> getAllImages();

    MediaDetailsResponse getImage( UUID id );

    List<MediaDetailsResponse> getImages( List<UUID> ids );

    boolean isImageExist( UUID id, MediaType mediaType );

    MediaResponse updateImage( UUID id, MediaUpdateRequest mediaUpdateRequest );

    void deleteImage( UUID id );

}
