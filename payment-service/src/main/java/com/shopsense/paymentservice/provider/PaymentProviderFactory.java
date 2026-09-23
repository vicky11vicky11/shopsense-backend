package com.shopsense.paymentservice.provider;

import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.exceptions.PaymentException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<PaymentGateway, PaymentProvider> providers;

    public PaymentProviderFactory( List<PaymentProvider> providerList ) {
        this.providers = new EnumMap<>(PaymentGateway.class);
        for ( PaymentProvider provider : providerList ) {
            providers.put(provider.getGateway(), provider);
        }
    }

    public PaymentProvider getProvider( PaymentGateway gateway ) {
        PaymentProvider provider = providers.get(gateway);
        if ( provider == null ) {
            throw new PaymentException("Unsupported payment gateway: " + gateway);
        }
        return provider;
    }
}