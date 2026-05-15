package com.car_pooling_system.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("sk_test_51TQ8UW2fQwmzlPc0HQ50sNJg4rxYYbjezp682eknOnu67Kv4IFIJk5tj7LgZM2Zf1rpAscohTjhxdPnNvcOAsUeX00LAVtNqsi")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}