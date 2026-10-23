package com.shopsense.commonservice.util;

public final class AppUrl {


    private AppUrl(){}

    //    Base URL
    private static final String BASE_URL = "/api/v1";

    //    Endpoints for this service
    public static final String MEDIA_URL = BASE_URL + "/media";

    public static final String PRODUCT_URL = BASE_URL + "/products";
    public static final String CATEGORY_URL = BASE_URL + "/categories";
    public static final String BRAND_URL = BASE_URL + "/brands";
    public static final String STORE_URL = BASE_URL + "/stores";

    public static final String PAYMENTS_URL = BASE_URL + "/payments";

    public static final String ORDER_URL = BASE_URL + "/orders";
    public static final String CART_URL = BASE_URL + "/cart";

    public static final String INVENTORY_URL  = BASE_URL + "/inventory";
    public static final String RESERVATION_URL  = BASE_URL + "/reservations";

    public static final String CUSTOMER_URL = BASE_URL + "/customers";
    public static final String SELLER_URL = BASE_URL + "/sellers";
    public static final String ADMIN_URL = BASE_URL + "/admins";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";

    public static final String NOTIFICATION_URL = BASE_URL + "/notifications";

    public static final String ORCHESTRATOR_URL = BASE_URL + "/orchestrator";
}

