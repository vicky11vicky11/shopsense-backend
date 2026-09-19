package com.shopsense.orderservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1";

    public static final String CART_URL = BASE_URL + "/cart";
    public static final String ORDER_URL = BASE_URL + "/orders";
    public static final String CUSTOMER_URL = BASE_URL + "/customers";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";
    public static final String PRODUCT_URL = BASE_URL + "/products";
    public static final String MEDIA_URL = BASE_URL + "/media";
    public static final String INVENTORY_URL  = BASE_URL + "/inventory";
    public static final String RESERVATION_URL  = BASE_URL + "/reservations";
}
