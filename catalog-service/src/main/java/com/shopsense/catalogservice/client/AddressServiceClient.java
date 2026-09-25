package com.shopsense.catalogservice.client;

import com.shopsense.catalogservice.util.AppUrl;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.ADDRESS_URL)
public interface AddressServiceClient {

    @GetExchange("/exists")
    boolean isAddressExists( @RequestParam UUID addressId, @RequestParam(required = false) UUID userId );

}
