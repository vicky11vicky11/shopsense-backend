package com.shopsense.mediaservice.controller;

import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaResponse;
import com.shopsense.mediaservice.service.CloudinaryService;
import com.shopsense.mediaservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrl.CLOUDINARY_MEDIA_URL)
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> uploadImage( @Valid @ModelAttribute MediaRequest mediaUploadRequest ) {
        MediaResponse response = cloudinaryService.uploadImage(mediaUploadRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<MediaResponse> getImage( @RequestParam String publicId ) {
        return ResponseEntity.ok(cloudinaryService.getImage(publicId));
    }

    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> updateImage( @RequestParam String publicId,
                                                      @Valid @ModelAttribute MediaUpdateRequest mediaUpdateRequest ) {
        return ResponseEntity.ok(cloudinaryService.updateImage(publicId, mediaUpdateRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImage( @RequestParam String publicId ) {
        cloudinaryService.deleteImage(publicId);
        return ResponseEntity.noContent()
                .build();
    }

}
