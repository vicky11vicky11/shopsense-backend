package com.shopsense.userservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1";

    public static final String CUSTOMER_URL = BASE_URL + "/customers";
    public static final String SELLER_URL = BASE_URL + "/sellers";
    public static final String ADMIN_URL = BASE_URL + "/admins";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";
}
