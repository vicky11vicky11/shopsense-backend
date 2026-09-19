package com.shopsense.catalogservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1";

    public static final String PRODUCT_URL = BASE_URL + "/products";
    public static final String CATEGORY_URL = BASE_URL + "/categories";
    public static final String BRAND_URL = BASE_URL + "/brands";
    public static final String STORE_URL = BASE_URL + "/stores";
    public static final String MEDIA_URL = BASE_URL + "/media";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";
    public static final String SELLER_URL = BASE_URL + "/sellers";

}
