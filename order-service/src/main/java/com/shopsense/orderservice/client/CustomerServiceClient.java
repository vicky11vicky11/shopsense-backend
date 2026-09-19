package com.shopsense.orderservice.client;

import com.shopsense.orderservice.utils.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(AppUrl.CUSTOMER_URL)
public interface CustomerServiceClient {

    @GetExchange("/exists/{customerId}")
    boolean isCustomerExists( @PathVariable String customerId );

}
