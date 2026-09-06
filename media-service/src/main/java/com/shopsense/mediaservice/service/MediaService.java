package com.shopsense.mediaservice.service;

import com.shopsense.mediaservice.request.BulkMediaUploadRequest;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaDetailsResponse;
import com.shopsense.mediaservice.response.MediaResponse;

import java.util.List;

public interface MediaService {

    MediaResponse uploadImage( MediaRequest mediaRequest );

    List<MediaResponse> uploadImages( BulkMediaUploadRequest mediaRequest );

    MediaDetailsResponse getImage( String id );

    List<MediaDetailsResponse> getImages( List<String> ids );

    MediaResponse updateImage( String id, MediaUpdateRequest mediaUpdateRequest );

    void deleteImage( String id );

}
