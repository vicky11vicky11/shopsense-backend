package com.shopsense.catalogservice.client;

import com.shopsense.catalogservice.enums.MediaType;
import com.shopsense.catalogservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.MEDIA_URL)
public interface MediaServiceClient {

    @GetExchange("/exist/{id}")
    boolean imageExist( @PathVariable UUID id, @RequestParam MediaType mediaType );

}
