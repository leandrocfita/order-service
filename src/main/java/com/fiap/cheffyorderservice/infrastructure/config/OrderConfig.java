package com.fiap.cheffyorderservice.infrastructure.config;

import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.services.PlaceOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

    @Bean
    public PlaceOrderService placeOrderService(
            OrderRepositoryOutputPort orderRepositoryOutputPort
    ) {
        return new PlaceOrderService(
                orderRepositoryOutputPort
        );
    }
}
