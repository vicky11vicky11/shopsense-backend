package com.shopsense.orderservice.client;

import com.shopsense.orderservice.utils.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.PRODUCT_URL)
public interface ProductServiceClient {

    @GetExchange("/exists/{productId}/active")
    boolean isProductExists( @PathVariable UUID productId );

}
