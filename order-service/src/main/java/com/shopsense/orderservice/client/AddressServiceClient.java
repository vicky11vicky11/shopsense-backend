package com.shopsense.orderservice.client;

import com.shopsense.orderservice.utils.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(AppUrl.ADDRESS_URL)
public interface AddressServiceClient {

    @GetExchange("/exists/{addressId}")
    boolean isAddressExists(@PathVariable("addressId") String addressId);

}
