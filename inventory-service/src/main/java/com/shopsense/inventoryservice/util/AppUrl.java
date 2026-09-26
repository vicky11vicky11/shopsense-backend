package com.shopsense.inventoryservice.util;

public class AppUrl {

    private AppUrl(){}

    //    Base URL
    private static final String BASE_URL = "/api/v1";

    //    Endpoints for this service
    public static final String INVENTORY_URL  = BASE_URL + "/inventory";
    public static final String RESERVATION_URL  = BASE_URL + "/reservations";

    //    Endpoints for Http Clients
    public static final String PRODUCT_URL = BASE_URL + "/products";
    public static final String ORDER_URL = BASE_URL + "/orders";

}
