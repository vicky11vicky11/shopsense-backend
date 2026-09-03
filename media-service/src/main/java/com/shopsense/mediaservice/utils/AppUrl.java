package com.shopsense.mediaservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1/media";

    public static final String CLOUDINARY_MEDIA_URL = BASE_URL + "/cloudinary";
    public static final String AWS_URL = BASE_URL + "/aws";
}
