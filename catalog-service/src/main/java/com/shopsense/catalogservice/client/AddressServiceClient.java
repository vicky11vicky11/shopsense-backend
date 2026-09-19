package com.shopsense.catalogservice.client;

import com.shopsense.catalogservice.utils.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(AppUrl.ADDRESS_URL)
public interface AddressServiceClient {

    @GetExchange("/exists/{addressId}")
    public boolean isAddressExists(@PathVariable String addressId);

}
