package com.fiap.cheffyorderservice.application.ports.out;

import com.fiap.cheffyorderservice.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryOutputPort {
    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);

    Order updateByOrderId(String orderId, Order order);
}
