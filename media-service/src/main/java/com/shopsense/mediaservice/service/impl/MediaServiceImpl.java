package com.shopsense.mediaservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shopsense.mediaservice.entity.Media;
import com.shopsense.mediaservice.enums.MediaType;
import com.shopsense.mediaservice.exceptions.InvalidFileException;
import com.shopsense.mediaservice.exceptions.MediaNotFoundException;
import com.shopsense.mediaservice.exceptions.MediaUploadException;
import com.shopsense.mediaservice.repository.MediaRepository;
import com.shopsense.mediaservice.request.BulkMediaUploadRequest;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaDetailsResponse;
import com.shopsense.mediaservice.response.MediaResponse;
import com.shopsense.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;

    private final MediaRepository mediaRepository;

    private final Executor mediaUploadExecutor;

    @Override
    public MediaResponse uploadImage( MediaRequest mediaRequest ) {
        MultipartFile image = mediaRequest.getImage();
        validateImage(image);
        log.info("Uploading single image with media type: {}", mediaRequest.getMediaType());
        return uploadSingleImage(image, mediaRequest.getMediaType());
    }

    @Override
    public List<MediaResponse> uploadImages( BulkMediaUploadRequest mediaRequest ) {
        List<MultipartFile> images = mediaRequest.getImages();
        images.forEach(this::validateImage);
        log.info("Uploading {} images with media type: {}", images.size(), mediaRequest.getMediaType());
        try {
            List<CompletableFuture<Media>> futures = images.stream()
                    .map(image -> CompletableFuture.supplyAsync(() -> uploadImageToCloudinary(image, mediaRequest.getMediaType())))
                    .toList();
            List<Media> mediaList = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            List<Media> savedMediaList = mediaRepository.saveAll(mediaList);
            log.info("{} media records saved successfully to MongoDB", savedMediaList.size());
            return savedMediaList.stream()
                    .map(media -> MediaResponse.builder()
                            .id(media.getId())
                            .build())
                    .toList();
        } catch ( Exception exception ) {
            log.error("Failed to upload multiple images", exception);
            throw new MediaUploadException("Failed to upload multiple images", exception);
        }
    }

    @Override
    public MediaDetailsResponse getImage( String id ) {
        Media media = findMediaById(id);
        return buildMediaDetailsResponse(media);
    }

    @Override
    public List<MediaDetailsResponse> getImages( List<String> ids ) {
        List<Media> mediaList = mediaRepository.findAllByIdIn(ids);
        return mediaList.stream()
                .map(this::buildMediaDetailsResponse)
                .toList();
    }

    @Override
    public MediaResponse updateImage( String id, MediaUpdateRequest mediaUpdateRequest ) {
        MultipartFile image = mediaUpdateRequest.getImage();
        validateImage(image);
        Media media = findMediaById(id);
        try {
            log.info("Updating media with ID: {}", id);
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(image.getBytes(), ObjectUtils.asMap("public_id", media.getPublicId(), "overwrite", true, "resource_type", "image"));
            updateMedia(media, uploadResult);
            Media updatedMedia = mediaRepository.save(media);
            log.info("Image updated successfully. Media ID: {}", updatedMedia.getId());
            return MediaResponse.builder()
                    .id(updatedMedia.getId())
                    .build();
        } catch ( IOException exception ) {
            log.error("Failed to update media with ID: {}", id, exception);
            throw new MediaUploadException("Failed to update image", exception);
        }
    }

    @Override
    public void deleteImage( String id ) {
        Media media = findMediaById(id);
        try {
            log.info("Deleting media with ID: {}", id);
            Map<?, ?> deleteResult = cloudinary.uploader()
                    .destroy(media.getPublicId(), ObjectUtils.asMap("resource_type", "image"));
            String result = (String) deleteResult.get("result");
            if ( !"ok".equals(result) ) {
                throw new MediaUploadException("Failed to delete image");
            }
            mediaRepository.delete(media);
            log.info("Image deleted successfully. Media ID: {}", id);
        } catch ( IOException exception ) {
            log.error("Failed to delete media with ID: {}", id, exception);
            throw new MediaUploadException("Failed to delete image", exception);
        }
    }

    private MediaResponse uploadSingleImage( MultipartFile image, MediaType mediaType ) {
        Media media = uploadImageToCloudinary(image, mediaType);
        Media savedMedia = mediaRepository.save(media);
        log.info("Image uploaded successfully. Media ID: {}", savedMedia.getId());
        return MediaResponse.builder()
                .id(savedMedia.getId())
                .build();
    }

    private Media uploadImageToCloudinary( MultipartFile image, MediaType mediaType ) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(image.getBytes(), ObjectUtils.asMap("folder", resolveFolder(mediaType), "resource_type", "image"));
            log.info("Image uploaded to Cloudinary. Public ID: {}", uploadResult.get("public_id"));
            return buildMedia(uploadResult, mediaType);
        } catch ( IOException exception ) {
            log.error("Failed to upload image to Cloudinary", exception);
            throw new MediaUploadException("Failed to upload image", exception);
        }
    }

    private Media findMediaById( String id ) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with ID: " + id));
    }

    private Media buildMedia( Map<?, ?> uploadResult, MediaType mediaType ) {
        return Media.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("url"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width(getIntegerValue(uploadResult.get("width")))
                .height(getIntegerValue(uploadResult.get("height")))
                .bytes(getLongValue(uploadResult.get("bytes")))
                .mediaType(mediaType)
                .build();
    }

    private void updateMedia( Media media, Map<?, ?> uploadResult ) {
        media.setPublicId((String) uploadResult.get("public_id"));
        media.setUrl((String) uploadResult.get("url"));
        media.setSecureUrl((String) uploadResult.get("secure_url"));
        media.setFormat((String) uploadResult.get("format"));
        media.setWidth(getIntegerValue(uploadResult.get("width")));
        media.setHeight(getIntegerValue(uploadResult.get("height")));
        media.setBytes(getLongValue(uploadResult.get("bytes")));
    }

    private MediaDetailsResponse buildMediaDetailsResponse( Media media ) {
        return MediaDetailsResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .secureUrl(media.getSecureUrl())
                .format(media.getFormat())
                .width(media.getWidth())
                .height(media.getHeight())
                .bytes(media.getBytes())
                .mediaType(media.getMediaType())
                .build();
    }

    private void validateImage( MultipartFile image ) {
        if ( image == null || image.isEmpty() ) {
            throw new InvalidFileException("Image cannot be empty");
        }
        String contentType = image.getContentType();
        String fileName = image.getOriginalFilename();
        boolean validContentType = "image/jpeg".equalsIgnoreCase(contentType) || "image/png".equalsIgnoreCase(contentType) || "image/webp".equalsIgnoreCase(contentType) || "image/gif".equalsIgnoreCase(contentType);
        boolean validExtension = fileName != null && ( fileName.toLowerCase(Locale.ROOT)
                .endsWith(".jpg") || fileName.toLowerCase(Locale.ROOT)
                .endsWith(".jpeg") || fileName.toLowerCase(Locale.ROOT)
                .endsWith(".png") || fileName.toLowerCase(Locale.ROOT)
                .endsWith(".webp") || fileName.toLowerCase(Locale.ROOT)
                .endsWith(".gif") );
        if ( !validContentType && !validExtension ) {
            throw new InvalidFileException("Only JPG, JPEG, PNG, WEBP and GIF images are allowed");
        }
    }

    private String resolveFolder( MediaType mediaType ) {
        return switch ( mediaType ) {
            case USER_PROFILE -> "shopsense/users";
            case PRODUCT -> "shopsense/products";
            case CATEGORY -> "shopsense/categories";
            case REVIEW -> "shopsense/reviews";
        };
    }

    private Integer getIntegerValue( Object value ) {
        return value != null ? ( (Number) value ).intValue() : null;
    }

    private Long getLongValue( Object value ) {
        return value != null ? ( (Number) value ).longValue() : null;
    }
}