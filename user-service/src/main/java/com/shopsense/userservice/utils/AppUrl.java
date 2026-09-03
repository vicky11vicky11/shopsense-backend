package com.shopsense.userservice.utils;

public class AppUrl {

    private AppUrl(){}

    private static final String BASE_URL = "/api/v1/user";

    public static final String USER_URL = BASE_URL + "/users";
    public static final String ADDRESS_URL = BASE_URL + "/addresses";
}
