package com.shopsense.inventoryservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1";

    public static final String INVENTORY_URL  = BASE_URL + "/inventory";
    public static final String RESERVATION_URL  = BASE_URL + "/reservations";
}
