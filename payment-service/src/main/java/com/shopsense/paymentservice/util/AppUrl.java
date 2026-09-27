package com.shopsense.paymentservice.util;

public class AppUrl {

    private AppUrl(){}

//    Base URL
    private static final String BASE_URL = "/api/v1";

//    Endpoints for this service
    public static final String PAYMENTS_URL = BASE_URL + "/payments";

    //    Endpoints for Http Clients
    public static final String ORDER_URL = BASE_URL + "/orders";

    public static final String CUSTOMER_URL = BASE_URL + "/customers";
}
