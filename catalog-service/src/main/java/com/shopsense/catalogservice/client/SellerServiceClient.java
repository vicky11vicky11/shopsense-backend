package com.shopsense.catalogservice.client;

import com.shopsense.catalogservice.utils.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(AppUrl.SELLER_URL)
public interface SellerServiceClient {

    @GetExchange("/exists/{sellerId}")
    boolean isSellerExists(@PathVariable String sellerId);

}
