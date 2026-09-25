package com.shopsense.userservice.util;

public class AppUrl {

    private AppUrl(){}

    //    Base URL
    private static final String BASE_URL = "/api/v1";

    //    Endpoints for this service
    public static final String CUSTOMER_URL = BASE_URL + "/customers";
    public static final String SELLER_URL = BASE_URL + "/sellers";
    public static final String ADMIN_URL = BASE_URL + "/admins";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";

    //    Endpoints for Http Clients
    public static final String MEDIA_URL = BASE_URL + "/media";

}
