package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.domain.model.Order;

import java.util.UUID;

public class PlaceOrderService implements PlaceOrderInputPort {
    private final OrderRepositoryOutputPort orderRepository;

    public PlaceOrderService(
            OrderRepositoryOutputPort orderRepositoryOutputPort
    ) {
        this.orderRepository = orderRepositoryOutputPort;
    }

    @Override
    public UUID execute(PlaceOrderCommandRecord request) {

        Order order = Order.create(
                request.orderId(),
                request.totalAmount(),
                request.currency(),
                request.status()
        );

        orderRepository.save(order);

        return order.getId();
    }
}
