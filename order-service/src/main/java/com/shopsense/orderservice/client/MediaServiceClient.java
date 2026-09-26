package com.shopsense.orderservice.client;

import com.shopsense.orderservice.response.MediaDetailsResponse;
import com.shopsense.orderservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.MEDIA_URL)
public interface MediaServiceClient {

    @GetExchange("/{mediaId}")
    MediaDetailsResponse  getMedia( @PathVariable UUID mediaId );
}
