package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.domain.model.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class PlaceOrderService implements PlaceOrderInputPort {
    private final OrderRepositoryOutputPort orderRepository;

    @Override
    public UUID execute(PlaceOrderCommandRecord request) {
        log.info("PlaceOrderService.execute - START - orderId: [{}]", request.orderId());

        Order order = Order.create(
                request.orderId(),
                request.totalAmount(),
                request.currency(),
                request.status()
        );

        orderRepository.save(order);

        log.info("PlaceOrderService.execute - END - orderId: [{}], status: [{}]",
                order.getOrderId(), order.getStatus()
        );

        return order.getId();
    }
}
