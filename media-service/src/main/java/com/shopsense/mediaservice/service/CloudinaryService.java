package com.shopsense.mediaservice.service;

import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaResponse;

public interface CloudinaryService {

    MediaResponse uploadImage(MediaRequest mediaUploadRequest);

    MediaResponse getImage(String publicId);

    MediaResponse updateImage(
            String publicId,
            MediaUpdateRequest mediaUpdateRequest
    );

    void deleteImage(String publicId);
}