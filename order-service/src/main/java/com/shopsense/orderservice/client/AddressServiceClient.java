package com.shopsense.orderservice.client;

import com.shopsense.orderservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.ADDRESS_URL)
public interface AddressServiceClient {

    @GetExchange("/exists")
    boolean isAddressExists(@RequestParam UUID addressId, @RequestParam(required = false) UUID userId);

}
