package com.shopsense.paymentservice.client;

import com.shopsense.commonservice.response.UserResponse;
import com.shopsense.commonservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.CUSTOMER_URL)
public interface CustomerServiceClient {

    @GetExchange("/{customerId}")
    UserResponse getUserById( @PathVariable UUID customerId);

}
