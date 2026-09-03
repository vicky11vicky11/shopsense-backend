package com.shopsense.mediaservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shopsense.mediaservice.enums.MediaType;
import com.shopsense.mediaservice.exceptions.InvalidFileException;
import com.shopsense.mediaservice.exceptions.MediaNotFoundException;
import com.shopsense.mediaservice.exceptions.MediaUploadException;
import com.shopsense.mediaservice.request.MediaRequest;
import com.shopsense.mediaservice.request.MediaUpdateRequest;
import com.shopsense.mediaservice.response.MediaResponse;
import com.shopsense.mediaservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public MediaResponse uploadImage( MediaRequest mediaRequest ) {
        MultipartFile image = mediaRequest.getImage();
        validateImage(image);
        try {
            log.info("Uploading image with media type: {}", mediaRequest.getMediaType());
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(image.getBytes(), ObjectUtils.asMap("folder", resolveFolder(mediaRequest.getMediaType()), "resource_type", "image"));
            log.info("Image uploaded successfully with public id: {}", uploadResult.get("public_id"));
            return buildMediaResponse(uploadResult, mediaRequest.getMediaType());
        } catch ( IOException exception ) {
            log.error("Failed to upload image", exception);
            throw new MediaUploadException("Failed to upload image", exception);
        }
    }

    @Override
    public MediaResponse getImage( String publicId ) {
        try {

            Map<?, ?> result = cloudinary.api()
                    .resource(publicId, ObjectUtils.asMap("resource_type", "image"));
            return MediaResponse.builder()
                    .publicId((String) result.get("public_id"))
                    .url((String) result.get("url"))
                    .secureUrl((String) result.get("secure_url"))
                    .format((String) result.get("format"))
                    .width((Integer) result.get("width"))
                    .height((Integer) result.get("height"))
                    .bytes(result.get("bytes") != null ? ( (Number) result.get("bytes") ).longValue() : null)
                    .mediaType(resolveMediaType(publicId))
                    .build();
        } catch ( Exception exception ) {
            log.error("Image not found with public id: {}", publicId, exception);
            throw new MediaNotFoundException("Image not found with public id: " + publicId);
        }
    }

    @Override
    public MediaResponse updateImage( String publicId, MediaUpdateRequest mediaUpdateRequest ) {
        MultipartFile image = mediaUpdateRequest.getImage();
        validateImage(image);
        MediaResponse existingMedia = getImage(publicId);
        try {
            log.info("Replacing image with public id: {}", publicId);
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(image.getBytes(), ObjectUtils.asMap("public_id", publicId, "overwrite", true, "resource_type", "image"));
            log.info("Image replaced successfully with public id: {}", publicId);
            return buildMediaResponse(uploadResult, existingMedia.getMediaType());
        } catch ( IOException exception ) {
            log.error("Failed to replace image with public id: {}", publicId, exception);
            throw new MediaUploadException("Failed to update image", exception);
        }
    }

    @Override
    public void deleteImage( String publicId ) {
        getImage(publicId);
        try {
            log.info("Deleting image with public id: {}", publicId);
            Map<?, ?> deleteResult = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            String result = (String) deleteResult.get("result");
            if ( !"ok".equals(result) ) {
                throw new MediaUploadException("Failed to delete image");
            }
            log.info("Image deleted successfully with public id: {}", publicId);
        } catch ( IOException exception ) {
            log.error("Failed to delete image with public id: {}", publicId, exception);
            throw new MediaUploadException("Failed to delete image", exception);
        }
    }

    private void validateImage( MultipartFile image ) {
        if ( image == null || image.isEmpty() ) {
            throw new InvalidFileException("Image cannot be empty");
        }
        String contentType = image.getContentType();
        String fileName = image.getOriginalFilename();
        boolean validContentType = "image/jpeg".equals(contentType) || "image/png".equals(contentType) || "image/webp".equals(contentType) || "image/gif".equals(contentType);
        boolean validExtension = fileName != null && ( fileName.toLowerCase()
                .endsWith(".jpg") || fileName.toLowerCase()
                .endsWith(".jpeg") || fileName.toLowerCase()
                .endsWith(".png") || fileName.toLowerCase()
                .endsWith(".webp") );
        if ( !validContentType && !validExtension ) {
            throw new InvalidFileException("Only JPG, JPEG, PNG, WEBP and GIF images are allowed");
        }
    }

    private String resolveFolder( MediaType mediaType ) {
        return switch ( mediaType ) {
            case USER_PROFILE -> "shopsense/users/profiles";
            case PRODUCT -> "shopsense/products";
            case CATEGORY -> "shopsense/categories";
            case REVIEW -> "shopsense/reviews";
        };
    }

    private MediaType resolveMediaType( String publicId ) {
        for ( MediaType mediaType : MediaType.values() ) {
            String folder = resolveFolder(mediaType);
            if ( publicId.startsWith(folder + "/") ) {
                return mediaType;
            }
        }
        throw new MediaNotFoundException("Unable to determine media type for public id: " + publicId);
    }

    private MediaResponse buildMediaResponse( Map<?, ?> uploadResult, MediaType mediaType ) {
        return MediaResponse.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("url"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width(( (Number) uploadResult.get("width") ).intValue())
                .height(( (Number) uploadResult.get("height") ).intValue())
                .bytes(( (Number) uploadResult.get("bytes") ).longValue())
                .mediaType(mediaType)
                .build();
    }

}
