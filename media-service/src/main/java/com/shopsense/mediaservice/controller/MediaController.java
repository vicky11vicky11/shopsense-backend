package com.shopsense.mediaservice.controller;

import com.shopsense.mediaservice.request.BulkMediaRequest;
import com.shopsense.mediaservice.request.BulkMediaUploadRequest;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaDetailsResponse;
import com.shopsense.mediaservice.response.MediaResponse;
import com.shopsense.mediaservice.service.MediaService;
import com.shopsense.mediaservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrl.MEDIA_URL)
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> uploadImage( @Valid @ModelAttribute MediaRequest mediaRequest ) {
        MediaResponse response = mediaService.uploadImage(mediaRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(value = "/bulk-upload", consumes = "multipart/form-data")
    public ResponseEntity<List<MediaResponse>> uploadImages( @Valid @ModelAttribute BulkMediaUploadRequest mediaRequest ) {
        List<MediaResponse> responseList = mediaService.uploadImages(mediaRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaDetailsResponse> getImage( @PathVariable String id ) {
        return ResponseEntity.ok(mediaService.getImage(id));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<MediaDetailsResponse>> getImages( @Valid @RequestBody BulkMediaRequest request ) {
        return ResponseEntity.ok(mediaService.getImages(request.getIds()));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> updateImage( @PathVariable String id, @Valid @ModelAttribute MediaUpdateRequest mediaRequest ) {
        return ResponseEntity.ok(mediaService.updateImage(id, mediaRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage( @PathVariable String id ) {
        mediaService.deleteImage(id);
        return ResponseEntity.noContent()
                .build();
    }

}
