package com.shopsense.gatewayservice.utils;

public class ServiceName {

    private ServiceName() {
    }

    public static final String CATALOG = "lb://catalog-service";
    public static final String INVENTORY = "lb://inventory-service";
    public static final String MEDIA = "lb://media-service";
    public static final String ORDER = "lb://order-service";
    public static final String USER = "lb://user-service";
}