package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.ports.in.CancelOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.OrderNotFoundException;
import com.fiap.cheffyorderservice.domain.model.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CancelOrderService implements CancelOrderInputPort {

    private final OrderRepositoryOutputPort orderRepository;
    private final OrderStatusChangeOutputPort orderStatusChangeOutputPort;


    @Override
    public void cancelOrder(String orderId) {

        log.info("Cancelando pedido com id: {}", orderId);

        Optional<Order> order = orderRepository.findByOrderId(orderId);

        if (order.isEmpty()) {
            log.error("Order not found with id: {}", orderId);
            return;
        }
        Order recoveredOrder = order.get();

        if(recoveredOrder.getStatus().equals(PaymentStatus.CANCELED)) {
            log.info("Pedido de id: {} já cancelado", orderId);
            return;
        }

        recoveredOrder.updateStatus(PaymentStatus.CANCELED);

        orderRepository.updateByOrderId(orderId, recoveredOrder);

        log.info("Pedido de id: {} cancelado", orderId);

        orderStatusChangeOutputPort.publishStatusChangeEvent(
                new OrderStatusOutputRecord(
                        recoveredOrder.getOrderId(),
                        PaymentStatus.CANCELED
                )
        );

    }
}
