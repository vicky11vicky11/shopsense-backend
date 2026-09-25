package com.shopsense.mediaservice.controller;

import com.shopsense.mediaservice.enums.MediaType;
import com.shopsense.mediaservice.request.BulkMediaRequest;
import com.shopsense.mediaservice.request.BulkMediaUploadRequest;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaDetailsResponse;
import com.shopsense.mediaservice.response.MediaResponse;
import com.shopsense.mediaservice.service.MediaService;
import com.shopsense.mediaservice.util.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
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

    @GetMapping("/bulk")
    public ResponseEntity<Map<MediaType,List<MediaDetailsResponse>>> getAllImages(){
        Map<MediaType,List<MediaDetailsResponse>> responseList = mediaService.getAllImages();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaDetailsResponse> getImage( @PathVariable  UUID mediaId ) {
        MediaDetailsResponse response = mediaService.getImage(mediaId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<MediaDetailsResponse>> getImages( @Valid @RequestBody BulkMediaRequest request ) {
        List<MediaDetailsResponse> responseList = mediaService.getImages(request.getIds());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/exist/{mediaId}")
    public ResponseEntity<Boolean> imageExist( @PathVariable  UUID mediaId, @RequestParam MediaType mediaType ) {
        boolean exist = mediaService.isImageExist(mediaId, mediaType);
        return ResponseEntity.ok(exist);
    }

    @PutMapping(value = "/{mediaId}", consumes = "multipart/form-data")
    public ResponseEntity<MediaResponse> updateImage( @PathVariable  UUID mediaId, @Valid @ModelAttribute MediaUpdateRequest mediaRequest ) {
        MediaResponse mediaResponse = mediaService.updateImage(mediaId, mediaRequest);
        return ResponseEntity.ok(mediaResponse);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteImage( @PathVariable  UUID mediaId ) {
        mediaService.deleteImage(mediaId);
        return ResponseEntity.noContent()
                .build();
    }

}
