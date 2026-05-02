package com.fiap.cheffyorderservice.application.ports.out;

import com.fiap.cheffyorderservice.domain.model.Order;

public interface OrderRepositoryOutputPort {
    Order save(Order order);
}
